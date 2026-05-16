// src/utils/jwt.js

/**
 * Декодирует payload JWT (без валидации подписи)
 * Используется только на фронте для чтения role, sub и т.д.
 */
export function decodeJwt(token) {
  if (!token) {
    throw new Error('JWT token is empty');
  }

  const parts = token.split('.');

  if (parts.length !== 3) {
    throw new Error('Invalid JWT format');
  }

  const base64Url = parts[1];

  // base64url → base64
  const base64 = base64Url
    .replace(/-/g, '+')
    .replace(/_/g, '/');

  try {
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(char =>
          '%' + ('00' + char.charCodeAt(0).toString(16)).slice(-2)
        )
        .join('')
    );

    return JSON.parse(jsonPayload);

  } catch (e) {
    throw new Error('Failed to decode JWT payload');
  }
}