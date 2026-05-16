import { Link } from 'react-router-dom';
import { AuthContext } from '../auth/AuthContext';
import { useContext } from 'react';
import { useNavigate } from 'react-router-dom';


export default function MasterMenu() {

  const auth = useContext(AuthContext);
  const navigate = useNavigate();
  return (
    <div>
      <h2>Меню мастера</h2>

      <p>Вы вошли как: {auth.user?.role}</p>

      <button onClick={() => navigate('/master/stock')}>
        Склад
      </button>
      
      <button onClick={() => navigate('/master/clients')}>
        Управление клиентами
      </button>

      <button onClick={() => navigate('/master/orders/create')}>
        Создать заказ
      </button>

      <button onClick={() => navigate('/master/orders')}>
        Мои заказы
      </button>


      <button onClick={()=>navigate("/master/warranty")}>
        Гарантийные обращения
      </button>

      <button onClick={() => navigate('/master/stats')}>
        Моя статистика
      </button>

      <button onClick={() => navigate('/master/tests')}>
        Сессии тестирования
      </button>

      <button onClick={auth.logout}>
        Выйти
      </button>
    </div>
  );
}