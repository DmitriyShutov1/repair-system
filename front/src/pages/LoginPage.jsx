// import React, { useState, useContext } from 'react';
// import { AuthContext } from '../auth/AuthContext';
// import funnyVideo from './funny.mp4';

// export default function LoginPage() {

//   const { login } = useContext(AuthContext);

//   const [form, setForm] = useState({
//     login: '',
//     password: ''
//   });

//   const [error, setError] = useState(null);

//   const handleSubmit = async (e) => {
//     e.preventDefault();

//     try {
//       await login(form.login, form.password);
//     } catch (err) {
//       setError('Ошибка входа');
//     }
//   };

//   return (
//     <div style={{
//       display: 'flex',
//       height: '100vh',
//       justifyContent: 'center',
//       alignItems: 'center',
//       background: '#0f172a'
//     }}>
//       <form
//         onSubmit={handleSubmit}
//         style={{
//           background: 'white',
//           padding: 40,
//           borderRadius: 12,
//           width: 350
//         }}
//       >
//         <h2>Вход в систему</h2>

//         {error && <p style={{ color: 'red' }}>{error}</p>}

//         <input
//           placeholder="Логин"
//           style={{ width: '100%', marginBottom: 10 }}
//           onChange={e => setForm({ ...form, login: e.target.value })}
//         />

//         <input
//           type="password"
//           placeholder="Пароль"
//           style={{ width: '100%', marginBottom: 20 }}
//           onChange={e => setForm({ ...form, password: e.target.value })}
//         />

//         <button style={{
//           width: '100%',
//           padding: 10,
//           background: '#2563eb',
//           color: 'white',
//           border: 'none',
//           borderRadius: 6
//         }}>
//           Войти
//         </button>
//       </form>
//     </div>
//   );
// }

// import React, { useState, useContext } from 'react';
// import { AuthContext } from '../auth/AuthContext';
// import funnyVideo from './funny.mp4'; // или './video/funny.mp4' - путь к твоему видео

// export default function LoginPage() {
//   const { login } = useContext(AuthContext);
//   const [form, setForm] = useState({ login: '', password: '' });
//   const [error, setError] = useState(null);

//   const handleSubmit = async (e) => {
//     e.preventDefault();
//     try {
//       await login(form.login, form.password);
//     } catch (err) {
//       setError('Ошибка входа');
//     }
//   };

//   return (
//     <div style={{
//       position: 'relative',
//       width: '100vw',
//       height: '100vh',
//       overflow: 'hidden'
//     }}>
//       {/* Видео на фоне */}
//       <video
//         autoPlay
//         loop          // это чтобы по кругу моталось
//         muted         // обязательно muted для автозапуска
//         playsInline
//         src={funnyVideo}
//         style={{
//           position: 'absolute',
//           top: '50%',
//           left: '50%',
//           transform: 'translate(-50%, -50%)',
//           minWidth: '100%',
//           minHeight: '100%',
//           width: 'auto',
//           height: 'auto',
//           objectFit: 'cover',  // видео покроет весь фон без искажений
//           zIndex: 0
//         }}
//       />

//       {/* Полупрозрачный слой, чтобы форма читалась (опционально) */}
//       <div style={{
//         position: 'absolute',
//         top: 0,
//         left: 0,
//         width: '100%',
//         height: '100%',
//         backgroundColor: 'rgba(0, 0, 0, 0.3)',  // легкое затемнение
//         zIndex: 1
//       }} />

//       {/* Форма */}
//       <div style={{
//         position: 'absolute',
//         top: '50%',
//         left: '50%',
//         transform: 'translate(-50%, -50%)',
//         zIndex: 2,
//         width: '100%',
//         display: 'flex',
//         justifyContent: 'center'
//       }}>
//         <form
//           onSubmit={handleSubmit}
//           style={{
//             background: 'white',
//             padding: 40,
//             borderRadius: 12,
//             width: 350
//           }}
//         >
//           <h2>Вход в систему</h2>
//           {error && <p style={{ color: 'red' }}>{error}</p>}

//           <input
//             placeholder="Логин"
//             style={{ width: '100%', marginBottom: 10, padding: 8 }}
//             onChange={e => setForm({ ...form, login: e.target.value })}
//           />

//           <input
//             type="password"
//             placeholder="Пароль"
//             style={{ width: '100%', marginBottom: 20, padding: 8 }}
//             onChange={e => setForm({ ...form, password: e.target.value })}
//           />

//           <button style={{
//             width: '100%',
//             padding: 10,
//             background: '#2563eb',
//             color: 'white',
//             border: 'none',
//             borderRadius: 6,
//             cursor: 'pointer'
//           }}>
//             Войти
//           </button>
//         </form>
//       </div>
//     </div>
//   );
// }

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
      backgroundColor: '#f0f2f5',  // ← обычный серый фон как в соцсетях
      // Или можно градиент:
      // background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      
      // Или просто белый:
      // backgroundColor: 'white',
    }}>
      
      {/* Форма */}
      <form
        onSubmit={handleSubmit}
        style={{
          background: 'white',
          padding: 40,
          borderRadius: 12,
          width: 350,
          boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',  // легкая тень
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