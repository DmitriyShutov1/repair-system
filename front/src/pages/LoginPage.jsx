import React, { useState, useContext } from 'react';
import { AuthContext } from '../auth/AuthContext';

export default function LoginPage() {
  const { login } = useContext(AuthContext);
  const [form, setForm] = useState({ login: '', password: '' });
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await login(form.login, form.password);
    } catch (err) {
      setError('Ошибка входа');
    }
  };

  return (
    <div style={{
      width: '100vw',
      height: '100vh',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      backgroundColor: '#f0f2f5',  

    }}>
      
       
      <form
        onSubmit={handleSubmit}
        style={{
          background: 'white',
          padding: 40,
          borderRadius: 12,
          width: 350,
          boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',   
        }}
      >
        <h2 style={{ marginTop: 0, marginBottom: 20, textAlign: 'center' }}>
          Вход в систему
        </h2>
        
        {error && <p style={{ color: 'red', textAlign: 'center' }}>{error}</p>}

        <input
          placeholder="Логин"
          style={{ 
            width: '100%', 
            marginBottom: 10, 
            padding: 8,
            border: '1px solid #ddd',
            borderRadius: 4
          }}
          onChange={e => setForm({ ...form, login: e.target.value })}
        />

        <input
          type="password"
          placeholder="Пароль"
          style={{ 
            width: '100%', 
            marginBottom: 20, 
            padding: 8,
            border: '1px solid #ddd',
            borderRadius: 4
          }}
          onChange={e => setForm({ ...form, password: e.target.value })}
        />

        <button style={{
          width: '100%',
          padding: 10,
          background: '#2563eb',
          color: 'white',
          border: 'none',
          borderRadius: 6,
          cursor: 'pointer',
          fontSize: 16
        }}>
          Войти
        </button>
      </form>
    </div>
  );
}