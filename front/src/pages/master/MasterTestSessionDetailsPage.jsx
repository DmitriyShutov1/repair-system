import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';
import { useParams, useSearchParams } from 'react-router-dom';

export default function MasterTestSessionDetailsPage(){

  const auth = useContext(AuthContext);

  const { sessionId } = useParams();
  const [searchParams] = useSearchParams();

  const orderId = searchParams.get('orderId');

  const [steps,setSteps] = useState([]);
  const [tests,setTests] = useState([]);

  const loadSteps = async () => {
    const data = await apiClient(
      `/api/orders/tests/sessions/${sessionId}/steps`,
      'GET',
      null,
      auth
    );

    if(data){
      setSteps(data);
    }
  };

  const loadTests = async () => {
    if(!orderId) return;

    const data = await apiClient(
      `/api/orders/tests`,
      'GET',
      null,
      auth
    );

    if(data){
      setTests(data);
    }
  };

  useEffect(()=>{
    loadSteps();
  },[]);

  return(
    <div>

      <h2>Сессия #{sessionId}</h2>

      <hr/>

      <h3>Этапы тестирования</h3>

      {steps.length === 0 ? (
        <p>Нет данных</p>
      ) : (
        <table border="1" cellPadding="5">
          <thead>
            <tr>
              <th>Step ID</th>
              <th>Test ID</th>
              <th>Пройден</th>
              <th>Дата</th>
            </tr>
          </thead>
          <tbody>
            {steps.map(s=>(
              <tr key={s.stepId}>
                <td>{s.stepId}</td>
                <td>{s.testId}</td>
                <td>{s.passed ? 'Да' : 'Нет'}</td>
                <td>{s.createdAt}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <hr/>

      <button onClick={loadTests}>
        Загрузить список тестов
      </button>

      {tests.length > 0 && (
        <div>
          <h3>Тесты</h3>

          <table border="1" cellPadding="5">
            <thead>
              <tr>
                <th>ID</th>
                <th>Название</th>
                <th>Описание</th>
              </tr>
            </thead>
            <tbody>
              {tests.map(t=>(
                <tr key={t.id}>
                  <td>{t.id}</td>
                  <td>{t.name}</td>
                  <td>{t.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

    </div>
  );
}