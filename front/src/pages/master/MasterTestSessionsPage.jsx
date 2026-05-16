import React, { useContext, useState } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';
import { useNavigate } from 'react-router-dom';

export default function MasterTestSessionsPage(){

  const auth = useContext(AuthContext);
  const navigate = useNavigate();

  const [orderId,setOrderId] = useState('');
  const [sessions,setSessions] = useState([]);

  const loadSessions = async () => {
    if(!orderId) return;

    const data = await apiClient(
      `/api/orders/${orderId}/tests/sessions`,
      'GET',
      null,
      auth
    );

    if(data){
      setSessions(data);
    }
  };

  return(
    <div>

      <h2>Сессии тестирования</h2>

      <input
        placeholder="ID заказа"
        value={orderId}
        onChange={e=>setOrderId(e.target.value)}
      />

      <button onClick={loadSessions}>
        Найти
      </button>

      <hr/>

      {sessions.length === 0 ? (
        <p>Нет данных</p>
      ) : (
        <table border="1" cellPadding="5">
          <thead>
            <tr>
              <th>Session ID</th>
              <th>Дата</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {sessions.map(s=>(
              <tr key={s.sessionId}>
                <td>{s.sessionId}</td>
                <td>{s.sessionAt}</td>
                <td>
                  <button
                    onClick={()=>navigate(`/master/tests/sessions/${s.sessionId}?orderId=${orderId}`)}
                  >
                    Подробнее
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

    </div>
  );
}