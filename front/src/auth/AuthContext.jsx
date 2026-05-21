import React, { createContext, useState, useEffect } from 'react';
import { decodeJwt } from '../utils/jwt';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {

  const [accessToken, setAccessToken] = useState('');
  const [user, setUser] = useState(null);
  const [deviceId, setDeviceId] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let stored = localStorage.getItem('deviceId');
    if (!stored) {
      stored = crypto.randomUUID();
      localStorage.setItem('deviceId', stored);
    }
    setDeviceId(stored);
  }, []);

  useEffect(() => {
    if (!deviceId) return;

    const tryRefresh = async () => {
      try {
        const res = await fetch('http://localhost:8080/api/auth/refresh', {
          method: 'POST',
          credentials: 'include',
          headers: {
            'X-Device-Id': deviceId
          }
        });

        if (!res.ok) throw new Error();

        const data = await res.json();

        setAccessToken(data.accessToken);

        const payload = decodeJwt(data.accessToken);

        setUser({
          id: payload.sub,
          role: payload.role,
          branchId: payload.branchId
        });

      } catch {
        setAccessToken('');
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    tryRefresh();
  }, [deviceId]);

  const login = async (login, password) => {

    const res = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Device-Id': deviceId
      },
      credentials: 'include',
      body: JSON.stringify({ login, password })
    });

    if (!res.ok) throw new Error('Login failed');

    const data = await res.json();

    setAccessToken(data.accessToken);

    const payload = decodeJwt(data.accessToken);

    setUser({
      id: payload.sub,
      role: payload.role,
      branchId: payload.branchId
    });
  };

  const logout = async () => {

    try {
      await fetch('http://localhost:8080/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Authorization': `Bearer ${accessToken}`,
          'X-Device-Id': deviceId
        }
      });
    } catch {}

    setAccessToken('');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{
      accessToken,
      user,
      login,
      logout,
      setAccessToken,
      loading
    }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};