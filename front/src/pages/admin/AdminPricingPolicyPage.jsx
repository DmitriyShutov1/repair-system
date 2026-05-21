import React, { useContext, useState } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';

export default function AdminPricingPolicyPage() {

  const auth = useContext(AuthContext);

  const [partId, setPartId] = useState('');
  const [serviceId, setServiceId] = useState('');

  const [costPrice, setCostPrice] = useState('');
  const [clientPrice, setClientPrice] = useState('');
  const [masterPercentage, setMasterPercentage] = useState('');

  const [results, setResults] = useState([]);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [pricelessParts, setPricelessParts] = useState([]);
  const [pricelessServices, setPricelessServices] = useState([]);

  const [partsPage, setPartsPage] = useState(0);
  const [partsTotalPages, setPartsTotalPages] = useState(0);

  const [servicesPage, setServicesPage] = useState(0);
  const [servicesTotalPages, setServicesTotalPages] = useState(0);

  const size = 10;

  const createPolicy = async () => {

    const data = await apiClient(
      '/api/pricing-policies',
      'POST',
      {
        partId: partId || null,
        serviceId: serviceId || null,
        costPrice,
        clientPrice,
        masterPercentage
      },
      auth
    );

    alert('Создана новая версия цены id=' + data.id);
  };

  const closePartPolicy = async () => {

    if (!window.confirm('Закрыть текущую цену для запчасти?')) return;

    await apiClient(
      `/api/pricing-policies/part/${partId}`,
      'DELETE',
      null,
      auth
    );

    alert('Цена закрыта');
  };

  const closeServicePolicy = async () => {

    if (!window.confirm('Закрыть текущую цену для услуги?')) return;

    await apiClient(
      `/api/pricing-policies/service/${serviceId}`,
      'DELETE',
      null,
      auth
    );

    alert('Цена закрыта');
  };

  const loadPartHistory = async (p = 0) => {

    const data = await apiClient(
      `/api/pricing-policies/part/${partId}?page=${p}&size=${size}`,
      'GET',
      null,
      auth
    );

    setResults(data.content);
    setPage(data.number);
    setTotalPages(data.totalPages);
  };

  const loadServiceHistory = async (p = 0) => {

    const data = await apiClient(
      `/api/pricing-policies/service/${serviceId}?page=${p}&size=${size}`,
      'GET',
      null,
      auth
    );

    setResults(data.content);
    setPage(data.number);
    setTotalPages(data.totalPages);
  };

  const loadActivePart = async () => {

    const data = await apiClient(
      `/api/pricing-policies/part/${partId}/active`,
      'GET',
      null,
      auth
    );

    setResults([data]);
  };

  const loadActiveService = async () => {

    const data = await apiClient(
      `/api/pricing-policies/service/${serviceId}/active`,
      'GET',
      null,
      auth
    );

    setResults([data]);
  };

  const loadPricelessParts = async (p = 0) => {

    const data = await apiClient(
      `/api/parts/pricelessParts?page=${p}&size=${size}`,
      'GET',
      null,
      auth
    );

    setPricelessParts(data.content);
    setPartsPage(data.number);
    setPartsTotalPages(data.totalPages);
  };

  const loadPricelessServices = async (p = 0) => {

    const data = await apiClient(
      `/api/services/pricelessServices?page=${p}&size=${size}`,
      'GET',
      null,
      auth
    );

    setPricelessServices(data.content);
    setServicesPage(data.number);
    setServicesTotalPages(data.totalPages);
  };

  return (

    <div>

      <h2>Ценовая политика</h2>

      <hr />

      <h3>Создать / обновить цену</h3>

      <div>

        <input
          placeholder="Part ID"
          value={partId}
          onChange={e => setPartId(e.target.value)}
        />

        <input
          placeholder="Service ID"
          value={serviceId}
          onChange={e => setServiceId(e.target.value)}
        />

      </div>

      <div>

        <input
          placeholder="Cost price"
          onChange={e => setCostPrice(e.target.value)}
        />

        <input
          placeholder="Client price"
          onChange={e => setClientPrice(e.target.value)}
        />

        <input
          placeholder="Master %"
          onChange={e => setMasterPercentage(e.target.value)}
        />

      </div>

      <button onClick={createPolicy}>
        Создать новую цену
      </button>

      <button onClick={()=>{
        setPartId('')
        setServiceId('')
      }}>
        Очистить
      </button>

      <hr />

      <h3>Активная цена</h3>

      <button onClick={loadActivePart}>
        Активная цена для запчасти
      </button>

      <button onClick={loadActiveService}>
        Активная цена для услуги
      </button>

      <hr />

      <h3>История цен</h3>

      <button onClick={() => loadPartHistory(0)}>
        История по запчасти
      </button>

      <button onClick={() => loadServiceHistory(0)}>
        История по услуге
      </button>

      <hr />

      <h3>Закрыть текущую цену</h3>

      <button onClick={closePartPolicy}>
        Закрыть цену для запчасти
      </button>

      <button onClick={closeServicePolicy}>
        Закрыть цену для услуги
      </button>

      <hr />

      <h3>Результаты</h3>

      {results.map(policy => (

        <div key={policy.id} style={{ border: '1px solid gray', margin: 10, padding: 10 }}>

          <p>ID: {policy.id}</p>
          <p>Part ID: {policy.partId}</p>
          <p>Service ID: {policy.serviceId}</p>

          <p>Cost price: {policy.costPrice}</p>
          <p>Client price: {policy.clientPrice}</p>
          <p>Master %: {policy.masterPercentage}</p>

          <p>Effective from: {policy.effectiveFrom}</p>
          <p>Effective to: {policy.effectiveTo || 'ACTIVE'}</p>

          <p>Version: {policy.version}</p>

        </div>

      ))}

      {totalPages > 1 && (

        <div>

          <button
            disabled={page === 0}
            onClick={() => {
              const newPage = page - 1;
              if (partId) loadPartHistory(newPage);
              if (serviceId) loadServiceHistory(newPage);
            }}
          >
            Назад
          </button>

          <span>
            Страница {page + 1} из {totalPages}
          </span>

          <button
            disabled={page + 1 >= totalPages}
            onClick={() => {
              const newPage = page + 1;
              if (partId) loadPartHistory(newPage);
              if (serviceId) loadServiceHistory(newPage);
            }}
          >
            Вперёд
          </button>

        </div>

      )}

      <hr />

      <h3>Запчасти без установленной цены</h3>

      <button onClick={() => loadPricelessParts(0)}>
        Показать список
      </button>

      {pricelessParts.map(p => (

        <div key={p.id} style={{border:'1px solid gray', margin:10, padding:10}}>

          <p>ID: {p.id}</p>
          <p>Название: {p.name}</p>
          <p>Артикул: {p.articleNumber}</p>

          {p.stock !== undefined && <p>На складе: {p.stock}</p>}
          {p.waiting !== undefined && <p>Ожидается: {p.waiting}</p>}

          <button onClick={() => setPartId(p.id)}>
            Установить цену
          </button>

        </div>

      ))}

      {partsTotalPages > 1 && (

        <div>

          <button
            disabled={partsPage === 0}
            onClick={() => loadPricelessParts(partsPage - 1)}
          >
            Назад
          </button>

          <span>
            Страница {partsPage + 1} из {partsTotalPages}
          </span>

          <button
            disabled={partsPage + 1 >= partsTotalPages}
            onClick={() => loadPricelessParts(partsPage + 1)}
          >
            Вперёд
          </button>

        </div>

      )}

      <hr />

      <h3>Услуги без установленной цены</h3>

      <button onClick={() => loadPricelessServices(0)}>
        Показать список
      </button>

      {pricelessServices.map(s => (

        <div key={s.id} style={{border:'1px solid gray', margin:10, padding:10}}>

          <p>ID: {s.id}</p>
          <p>Название: {s.name}</p>
          <p>Код услуги: {s.serviceCode}</p>
          <p>Категория: {s.category}</p>

          <button onClick={() => setServiceId(s.id)}>
            Установить цену
          </button>

        </div>

      ))}

      {servicesTotalPages > 1 && (

        <div>

          <button
            disabled={servicesPage === 0}
            onClick={() => loadPricelessServices(servicesPage - 1)}
          >
            Назад
          </button>

          <span>
            Страница {servicesPage + 1} из {servicesTotalPages}
          </span>

          <button
            disabled={servicesPage + 1 >= servicesTotalPages}
            onClick={() => loadPricelessServices(servicesPage + 1)}
          >
            Вперёд
          </button>

        </div>

      )}

    </div>
  );
}