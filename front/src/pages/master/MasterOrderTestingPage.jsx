import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';
import { useParams, useNavigate } from 'react-router-dom';

export default function MasterOrderTestingPage(){

  const auth = useContext(AuthContext);
  const navigate = useNavigate();

  const { orderId } = useParams();

  const [tests,setTests] = useState([]);
  const [results,setResults] = useState({}); // testId -> boolean

  // =========================
  // LOAD TESTS
  // =========================

  const loadTests = async () => {
    const data = await apiClient(
      `/api/orders/tests`,
      'GET',
      null,
      auth
    );

    if(data){
      setTests(data);

      // инициализация результатов
      const initial = {};
      data.forEach(t => {
        initial[t.id] = false;
      });
      setResults(initial);
    }
  };

  useEffect(()=>{
    loadTests();
  },[]);

  // =========================
  // TOGGLE CHECKBOX
  // =========================

  const toggleResult = (testId) => {
    setResults(prev => ({
      ...prev,
      [testId]: !prev[testId]
    }));
  };

  // =========================
  // SUBMIT RESULTS
  // =========================

  const submitResults = async () => {

    const payload = Object.keys(results).map(id => ({
      testId: Number(id),
      passed: results[id]
    }));

    await apiClient(
      `/api/orders/${orderId}/tests/results`,
      'POST',
      payload,
      auth
    );

    alert("Результаты тестирования сохранены");

    // можно вернуть назад
    navigate(`/master/orders/${orderId}`);
  };

  // =========================
  // UI
  // =========================

  return(
    <div>

      <h2>Тестирование заказа #{orderId}</h2>

      <hr/>

      {tests.length === 0 ? (
        <p>Нет тестов</p>
      ) : (
        <table border="1" cellPadding="5">
          <thead>
            <tr>
              <th>ID</th>
              <th>Название</th>
              <th>Описание</th>
              <th>Пройден</th>
            </tr>
          </thead>
          <tbody>
            {tests.map(t=>(
              <tr key={t.id}>
                <td>{t.id}</td>
                <td>{t.name}</td>
                <td>{t.description}</td>
                <td>
                  <input
                    type="checkbox"
                    checked={results[t.id] || false}
                    onChange={()=>toggleResult(t.id)}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <hr/>

      <button onClick={submitResults}>
        Отправить результаты
      </button>

    </div>
  );
}