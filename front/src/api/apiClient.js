// let isRefreshing = false;
// let refreshPromise = null;

// export const apiClient = async (url, method, body, auth) => {

//   const makeRequest = async (overrideToken = null) => {
//     const tokenToUse = overrideToken || auth.accessToken;

//     const response = await fetch(`http://localhost:8080${url}`, {
//       method,
//       credentials: 'include',
//       headers: {
//         'Content-Type': 'application/json',
//         'Authorization': tokenToUse ? `Bearer ${tokenToUse}` : undefined,
//         'X-Device-Id': localStorage.getItem('deviceId')
//       },
//       body: body ? JSON.stringify(body) : null
//     });

//     if (response.status === 401) {
//       // Пробуем прочитать тело ответа
//       let errorData = null;
//       try {
//         errorData = await response.clone().json();
//       } catch {
//         // не JSON — игнорируем
//       }

//       //const isTokenExpired = errorData?.error?.toLowerCase() === 'token_expired';
//       //const isTokenExpired = errorData?.message?.toLowerCase() === 'token_expired';
//       const isTokenExpired =
//         errorData?.message?.toLowerCase() === 'token_expired' ||
//         errorData?.error?.toLowerCase() === 'token_expired';

//       // 🔹 Если это сам /refresh
//       if (url.includes('/refresh')) {
//         auth.logout();
//         throw new Error('Refresh failed or expired');
//       }

//       // 🔹 Если токен истёк, делаем refresh и повторяем запрос с новым токеном
//       if (isTokenExpired) {
//         const newToken = await handleRefresh(auth);
//         return makeRequest(newToken);
//       }

//       // 🔹 Любая другая 401
//       auth.logout();
//       throw new Error('Authentication failed');
//     }

//     if (!response.ok) {
//       throw new Error(`API error ${response.status}`);
//     }

//     if (response.status === 204) return null;

//     return response.json();
//   };

//   return makeRequest();
// };

// const handleRefresh = async (auth) => {
//   if (!isRefreshing) {
//     isRefreshing = true;

//     refreshPromise = fetch('http://localhost:8080/api/auth/refresh', {
//       method: 'POST',
//       credentials: 'include',
//       headers: {
//         'X-Device-Id': localStorage.getItem('deviceId')
//       }
//     })
//       .then(res => {
//         if (!res.ok) throw new Error('Refresh failed');
//         return res.json();
//       })
//       .then(data => {
//         auth.setAccessToken(data.accessToken); // обновляем state
//         return data.accessToken;               // и возвращаем токен для повторного запроса
//       })
//       .finally(() => {
//         isRefreshing = false;
//       });
//   }

//   return refreshPromise;
// };

// let isRefreshing = false;
// let refreshPromise = null;

// // 🔥 глобальный обработчик ошибок (можешь подписаться в App)
// let globalErrorHandler = null;

// export const setGlobalErrorHandler = (handler) => {
//   globalErrorHandler = handler;
// };

// export const apiClient = async (url, method, body, auth) => {

//   const makeRequest = async (overrideToken = null) => {
//     const tokenToUse = overrideToken || auth.accessToken;

//     const response = await fetch(`http://localhost:8080${url}`, {
//       method,
//       credentials: 'include',
//       headers: {
//         'Content-Type': 'application/json',
//         'Authorization': tokenToUse ? `Bearer ${tokenToUse}` : undefined,
//         'X-Device-Id': localStorage.getItem('deviceId')
//       },
//       body: body ? JSON.stringify(body) : null
//     });

//     // =========================
//     // 🔴 401 (AUTH)
//     // =========================
//     if (response.status === 401) {
//       let errorData = null;

//       try {
//         errorData = await response.clone().json();
//       } catch {}

//       const isTokenExpired =
//         errorData?.message?.toLowerCase() === 'token_expired' ||
//         errorData?.error?.toLowerCase() === 'token_expired';

//       // если refresh сам упал → logout
//       if (url.includes('/refresh')) {
//         auth.logout();
//         throw new Error('Session expired');
//       }

//       // 🔁 refresh flow
//       if (isTokenExpired) {
//         const newToken = await handleRefresh(auth);
//         return makeRequest(newToken);
//       }

//       auth.logout();
//       throw new Error(errorData?.message || 'Unauthorized');
//     }

//     // =========================
//     // 🔴 ЛЮБЫЕ ДРУГИЕ ОШИБКИ
//     // =========================
//     if (!response.ok) {
//       let errorData = null;

//       try {
//         errorData = await response.clone().json();
//       } catch {}

//       const message =
//         errorData?.message ||
//         errorData?.error ||
//         `API error ${response.status}`;

//       // 🔥 глобально показываем
//       if (globalErrorHandler) {
//         globalErrorHandler(message);
//       }

//       throw new Error(message);
//     }

//     if (response.status === 204) return null;

//     return response.json();
//   };

//   return makeRequest();
// };

// // =========================
// // 🔁 REFRESH
// // =========================
// const handleRefresh = async (auth) => {
//   if (!isRefreshing) {
//     isRefreshing = true;

//     refreshPromise = fetch('http://localhost:8080/api/auth/refresh', {
//       method: 'POST',
//       credentials: 'include',
//       headers: {
//         'X-Device-Id': localStorage.getItem('deviceId')
//       }
//     })
//       .then(res => {
//         if (!res.ok) throw new Error('Refresh failed');
//         return res.json();
//       })
//       .then(data => {
//         auth.setAccessToken(data.accessToken);
//         return data.accessToken;
//       })
//       .finally(() => {
//         isRefreshing = false;
//       });
//   }

//   return refreshPromise;
// };

let isRefreshing = false;
let refreshPromise = null;

// глобальный обработчик ошибок
let globalErrorHandler = null;

export const setGlobalErrorHandler = (handler) => {
  globalErrorHandler = handler;
};

export const apiClient = async (url, method, body, auth) => {

  const makeRequest = async (overrideToken = null) => {
    const tokenToUse = overrideToken || auth.accessToken;

    const response = await fetch(`http://localhost:8080${url}`, {
      method,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': tokenToUse ? `Bearer ${tokenToUse}` : undefined,
        'X-Device-Id': localStorage.getItem('deviceId')
      },
      body: body ? JSON.stringify(body) : null
    });

    // =========================
    // 401
    // =========================
    if (response.status === 401) {
      let errorData = null;

      try {
        errorData = await response.clone().json();
      } catch {}

      const isTokenExpired =
        errorData?.message?.toLowerCase() === 'token_expired' ||
        errorData?.error?.toLowerCase() === 'token_expired';

      // refresh endpoint сам упал
      if (url.includes('/refresh')) {
        auth.logout();

        if (globalErrorHandler) {
          globalErrorHandler('Session expired');
        }

        return null;
      }

      // refresh flow
      if (isTokenExpired) {
        const newToken = await handleRefresh(auth);
        return makeRequest(newToken);
      }

      auth.logout();

      if (globalErrorHandler) {
        globalErrorHandler(errorData?.message || 'Unauthorized');
      }

      return null;
    }

    // =========================
    // ВСЕ ОСТАЛЬНЫЕ ОШИБКИ
    // =========================
    if (!response.ok) {
      let errorData = null;

      try {
        errorData = await response.clone().json();
      } catch {}

      const message =
        errorData?.message ||
        errorData?.error ||
        `API error ${response.status}`;

      if (globalErrorHandler) {
        globalErrorHandler(message);
      }

      return null; // ❗ НЕ THROW
    }

    if (response.status === 204) return null;

    return response.json();
  };

  return makeRequest();
};

// refresh
const handleRefresh = async (auth) => {
  if (!isRefreshing) {
    isRefreshing = true;

    refreshPromise = fetch('http://localhost:8080/api/auth/refresh', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'X-Device-Id': localStorage.getItem('deviceId')
      }
    })
      .then(res => {
        if (!res.ok) throw new Error('Refresh failed');
        return res.json();
      })
      .then(data => {
        auth.setAccessToken(data.accessToken);
        return data.accessToken;
      })
      .finally(() => {
        isRefreshing = false;
      });
  }

  return refreshPromise;
};