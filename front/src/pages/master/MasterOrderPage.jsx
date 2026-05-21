import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';
import { useParams, useNavigate } from 'react-router-dom';

export default function MasterOrderPage() {

  const auth = useContext(AuthContext);
  const navigate = useNavigate();

  const { orderId } = useParams();

  const [order, setOrder] = useState(null);
  const [cancelledByClient, setCancelledByClient] = useState(false);
  const [itemsToRemove, setItemsToRemove] = useState('');
  const [pickupCode, setPickupCode] = useState('');

  const loadOrder = async () => {
    const data = await apiClient(
      `/api/orders/info?orderId=${orderId}`,
      'POST',
      null,
      auth
    );
    setOrder(data);
  };

  useEffect(() => {
    loadOrder();
  }, []);

  const completeOrder = async () => {
    await apiClient(
      `/api/orders/${orderId}/complete`,
      'POST',
      null,
      auth
    );
    alert("Заказ завершен");
    loadOrder();
  };

  const issueOrder = async () => {
    await apiClient(
      `/api/orders/${orderId}/issue?pickupCode=${pickupCode}`,
      'POST',
      null,
      auth
    );
    alert("Заказ выдан");
    loadOrder();
  };

  const cancelOrder = async () => {
    const items = itemsToRemove
      ? itemsToRemove.split(',').map(i => Number(i.trim()))
      : [];

    await apiClient(
      `/api/orders/${orderId}/cancel?cancelledByClient=${cancelledByClient}`,
      'POST',
      items,
      auth
    );
    alert("Заказ отменен");
    loadOrder();
  };

  const removeSingleItem = async (itemId) => {
    if (!window.confirm("Удалить эту позицию?")) return;

    try {
      await apiClient(
        `/api/orders/${orderId}/items`,
        'DELETE',
        [itemId],
        auth
      );
      alert("Позиция удалена");
      loadOrder();
    } catch (e) {
      alert("Ошибка: " + e.message);
    }
  };


  const downloadRepairAct = async () => {

    try {

      const response = await fetch(
        `http://localhost:8080/api/orders/${orderId}/repair-act`,
        {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${auth.accessToken}`
          }
        }
      );

      if (!response.ok) {
        throw new Error('Ошибка скачивания PDF');
      }

      const blob = await response.blob();

      const url = window.URL.createObjectURL(blob);

      const a = document.createElement('a');

      a.href = url;

      a.download = `repair-act-${orderId}.pdf`;

      document.body.appendChild(a);

      a.click();

      a.remove();

      window.URL.revokeObjectURL(url);

    } catch (e) {

      alert(e.message);

    }
  };

  if (!order) {
    return <div>Loading...</div>;
  }

  const issued = order.status === "ISSUED";
  const completedOrIssued = order.status === "COMPLETED" || order.status === "ISSUED";

  return (

    <div>

      <h2>Заказ #{order.orderId}</h2>

      <hr />

      <p>Клиент: {order.clientId}</p>
      <p>Мастер: {order.masterId}</p>
      <p>Статус: {order.status}</p>
      <p>Гарантия: {order.warrantyId || 'Нет'}</p>

      <p>Серийный номер: {order.deviceSerial || 'Не указан'}</p>
      <p>Модель устройства: {order.deviceModel || 'Не указана'}</p>

      <p>Диагностика:</p>
      <p>{order.diagnosticResult}</p>

      <p>Создан: {order.createdAt}</p>

      {order.completedAt && (
        <p>Завершен: {order.completedAt}</p>
      )}

      
      {completedOrIssued && (
        <div style={{ marginTop: 10 }}>
          <button
            onClick={downloadRepairAct}
            style={{ background: '#4CAF50', color: 'white', padding: '8px 16px' }}
          >
            Скачать акт о ремонте (PDF)
          </button>
        </div>
      )}

      <hr />

      <h3>Позиции ремонта</h3>

      {(!order.items || order.items.length === 0) ? (

        <div>
          <p>Список деталей и услуг не установлен</p>
        </div>

      ) : (

        <div>
          {order.items.map(i => (
            <div key={i.id} style={{ border: '1px solid gray', margin: 5, padding: 5 }}>
              <p>Номер позиции: {i.id}</p>
              <p>Название: {i.name}</p>
              <p>Тип: {i.itemType}</p>
              <p>Категория: {i.category}</p>
              <p>Артикул: {i.articleNumber}</p>
              <p>Количество: {i.quantity}</p>
              <p>Цена: {i.sellPrice}</p>

              {!issued && (
                <button
                  onClick={() => removeSingleItem(i.id)}
                  style={{ background: 'red', color: 'white', marginTop: 5 }}
                >
                  Удалить позицию
                </button>
              )}
            </div>
          ))}
        </div>

      )}

      {!issued && (
        <div style={{ marginTop: 10 }}>
          <button onClick={() => navigate(`/master/orders/${orderId}/items`)}>
            {order.items && order.items.length > 0 ? 'Изменить позиции' : 'Добавить позиции'}
          </button>
        </div>
      )}

      {!issued && (

        <div>

          <hr />

          <h3>Завершение заказа</h3>

          <button onClick={() => navigate(`/master/orders/${orderId}/testing`)}>
            Заключительное тестирование
          </button>

          <hr />

          <h3>Выдача заказа</h3>

          <input
            placeholder="Pickup code"
            value={pickupCode}
            onChange={e => setPickupCode(e.target.value)}
          />

          <button onClick={issueOrder}>
            Выдать заказ
          </button>

        </div>

      )}

      {!issued && order.warrantyId == null && (
        <div>
          <hr />
          <h3>Отмена заказа</h3>

          <label>
            Клиент отменил
            <input
              type="checkbox"
              checked={cancelledByClient}
              onChange={e => setCancelledByClient(e.target.checked)}
            />
          </label>

          <p>ID позиций для удаления (через запятую)</p>

          <input
            value={itemsToRemove}
            onChange={e => setItemsToRemove(e.target.value)}
          />

          <button onClick={cancelOrder}>
            Отменить заказ
          </button>
        </div>
      )}

    </div>

  );
}