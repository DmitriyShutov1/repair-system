import { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../auth/AuthContext';

export default function AdminMenu() {

  const auth = useContext(AuthContext);
  const navigate = useNavigate();

  return (
    <div>
      <h1>Админ панель</h1>

      <p>Вы вошли как: {auth.user?.role}</p>

      <button onClick={() => navigate('/admin/users')}>
        Управление пользователями
      </button>

      <button onClick={() => navigate('/admin/branches')}>
        Управление филиалами
      </button>

      <button onClick={() => navigate('/admin/parts')}>
        Управление запчастями
      </button>

      <button onClick={() => navigate('/admin/services')}>
        Управление услугами
      </button>

      
      <button onClick={() => navigate('/admin/prices')}>
        Управление ценовой политикой
      </button>

      <button onClick={() => navigate('/admin/stats')}>
        Статистика
      </button>

      <br /><br />

      <button onClick={auth.logout}>
        Выйти
      </button>
    </div>
  );
}