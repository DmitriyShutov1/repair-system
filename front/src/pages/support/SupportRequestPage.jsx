import React, { useEffect, useState, useContext } from "react";
import { useParams } from "react-router-dom";
import { AuthContext } from "../../auth/AuthContext";
import { apiClient } from "../../api/apiClient";

export default function SupportRequestPage() {

  const { id } = useParams();
  const auth = useContext(AuthContext);

  const [request, setRequest] = useState(null);

  const loadRequest = async () => {

    const data = await apiClient(
      `/api/support-requests/${id}/info`,
      "GET",
      null,
      auth
    );

    setRequest(data);
  };

  useEffect(() => {
    loadRequest();
  }, []);

  const requireReturn = async () => {

    await apiClient(
      `/api/support-requests/${id}/require-return`,
      "POST",
      null,
      auth
    );

    alert("Возврат запрошен");
    loadRequest();
  };

  const setWarrantyNeeded = async () => {
    await apiClient(
      `/api/support-requests/warrantyNeeded/${id}`,
      "POST",
      null,
      auth
    );
    alert("Запрошен гарантийный ремонт");
    loadRequest();
  };

  const confirmReturn = async () => {

    await apiClient(
      `/api/support-requests/${id}/confirm-return`,
      "POST",
      null,
      auth
    );

    alert("Возврат подтвержден");
    loadRequest();
  };

  if (!request) return <div>Loading...</div>;

  return (

    <div>

      <h2>Обращение #{request.id}</h2>

      <p>Заказ: {request.orderId}</p>
      <p>Мастер: {request.masterId}</p>
      <p>Клиент: {request.clientId}</p>
      <p>Статус: {request.status}</p>
      <p>Филиал: {request.branchId}</p>
      <p>Серийный номер: {request.deviceSerial || 'Не указан'}</p>       
      <p>Модель устройства: {request.deviceModel || 'Не указана'}</p>    
      <p>Описание: {request.description}</p>
      <p>Создано: {request.createdAt}</p>

      {request.completedAt && (
        <p>Завершено: {request.completedAt}</p>
      )}

      <hr />

      <h3>Проблемные позиции</h3>

      {request.items.map(i =>

        <div key={i.id} style={{ border: "1px solid gray", margin: 5, padding: 5 }}>

          <b>{i.name}</b>

          <p>Тип: {i.itemType}</p>
          <p>Категория: {i.category}</p>
          <p>Количество: {i.quantity}</p>
          <p>Цена: {i.sellPrice}</p>

        </div>

      )}

      <hr />

      <h3>Управление</h3>

      <button onClick={requireReturn}>
        Запросить возврат
      </button>

      <button onClick={confirmReturn}>
        Подтвердить возврат
      </button>

      <button onClick={setWarrantyNeeded}>
        Запросить гарантийный ремонт
      </button>

    </div>

  );
}