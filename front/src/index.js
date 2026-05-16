import React from 'react';
import ReactDOM from 'react-dom/client';
import { AuthProvider } from './auth/AuthContext';
import App from './app/App';
import './index.css';
import { NotificationProvider } from './NotificationContext';

const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
  <NotificationProvider>
    <AuthProvider>
      <App />
    </AuthProvider>
  </NotificationProvider>
);