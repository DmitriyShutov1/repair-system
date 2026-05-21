
let isRefreshing = false;
let refreshPromise = null;

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

    if (response.status === 401) {
      let errorData = null;

      try {
        errorData = await response.clone().json();
      } catch {}

      const isTokenExpired =
        errorData?.message?.toLowerCase() === 'token_expired' ||
        errorData?.error?.toLowerCase() === 'token_expired';

      if (url.includes('/refresh')) {
        auth.logout();

        if (globalErrorHandler) {
          globalErrorHandler('Session expired');
        }

        return null;
      }

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

      return null; 
    }

    if (response.status === 204) return null;

    return response.json();
  };

  return makeRequest();
};

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