import React, { createContext, useState } from 'react';

export const NotificationContext = createContext(null);

export const NotificationProvider = ({ children }) => {
  const [message, setMessage] = useState(null);

  const showError = (text) => {
    setMessage(text);

    setTimeout(() => {
      setMessage(null);
    }, 4000);
  };

  return (
    <NotificationContext.Provider value={{ showError }}>
      {children}

      {message && (
        <div style={{
          position: 'fixed',
          top: 20,
          right: 20,
          background: '#ff4d4f',
          color: 'white',
          padding: '10px 15px',
          borderRadius: '6px',
          zIndex: 9999
        }}>
          {message}
        </div>
      )}
    </NotificationContext.Provider>
  );
};