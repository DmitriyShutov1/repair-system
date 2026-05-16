// import React,{useState,useContext} from "react"
// import {AuthContext} from "../../auth/AuthContext"
// import {apiClient} from "../../api/apiClient"

// export default function SupportCreateRequestPage(){

// const auth = useContext(AuthContext)

// const [orderId,setOrderId] = useState("")
// const [order,setOrder] = useState(null)

// const [selectedItems,setSelectedItems] = useState([])

// const [description,setDescription] = useState("")

// // ======================
// // LOAD ORDER
// // ======================

// const searchOrder = async()=>{

// const data = await apiClient(
// `/api/orders/info?orderId=${orderId}`,
// "POST",
// null,
// auth
// )

// setOrder(data)
// setSelectedItems([])

// }

// // ======================
// // SELECT ITEM
// // ======================

// const toggleItem = (item)=>{

// const exists = selectedItems.find(i=>i.id===item.id)

// if(exists){
// setSelectedItems(
// selectedItems.filter(i=>i.id!==item.id)
// )
// }else{
// setSelectedItems([...selectedItems,item])
// }

// }

// // ======================
// // CREATE REQUEST
// // ======================

// const createRequest = async()=>{

// if(selectedItems.length===0){
// alert("Выберите проблемные позиции")
// return
// }

// await apiClient(
// "/api/support-requests",
// "POST",
// {
// masterId:order.masterId,
// orderId:order.orderId,
// clientId:order.clientId,
// description,
// items:selectedItems
// },
// auth
// )

// alert("Обращение создано")

// setOrder(null)
// setOrderId("")
// setSelectedItems([])
// setDescription("")

// }

// // ======================
// // UI
// // ======================

// return(

// <div>

// <h2>Создание обращения поддержки</h2>

// <hr/>

// <h3>Поиск заказа</h3>

// <input
// placeholder="Введите ID заказа"
// value={orderId}
// onChange={e=>setOrderId(e.target.value)}
// />

// <button onClick={searchOrder}>
// Найти заказ
// </button>

// <hr/>

// {order && (

// <div>

// <h3>Информация о заказе</h3>

// <p>ID заказа: {order.orderId}</p>

// <p>Клиент: {order.clientId}</p>

// <p>Мастер: {order.masterId}</p>

// <p>Статус: {order.status}</p>

// <p>Завершено: {order.completedAt}</p>

// <p>Диагностика: {order.diagnosticResult}</p>

// <hr/>

// <h3>Позиции заказа</h3>

// {order.items.length===0 && (
// <p>В заказе нет позиций</p>
// )}

// {order.items.map(item=>

// <div key={item.id} style={{border:"1px solid gray",margin:5,padding:5}}>

// <input
// type="checkbox"
// checked={selectedItems.some(i=>i.id===item.id)}
// onChange={()=>toggleItem(item)}
// />

// <b>{item.name}</b>

// <p>Тип: {item.itemType}</p>

// <p>Категория: {item.category}</p>

// {item.quantity && (
// <p>Количество: {item.quantity}</p>
// )}

// <p>Цена: {item.sellPrice}</p>

// </div>

// )}

// <hr/>

// <h3>Описание проблемы</h3>

// <textarea
// value={description}
// onChange={e=>setDescription(e.target.value)}
// rows={4}
// cols={50}
// />

// <br/>

// <button onClick={createRequest}>
// Создать обращение
// </button>

// </div>

// )}

// </div>

// )

// }


import React, { useState, useContext } from "react";
import { AuthContext } from "../../auth/AuthContext";
import { apiClient } from "../../api/apiClient";

export default function SupportCreateRequestPage() {

  const auth = useContext(AuthContext);

  const [orderId, setOrderId] = useState("");
  const [order, setOrder] = useState(null);

  const [selectedItems, setSelectedItems] = useState([]);

  const [description, setDescription] = useState("");

  // ======================
  // LOAD ORDER
  // ======================

  const searchOrder = async () => {

    const data = await apiClient(
      `/api/orders/info?orderId=${orderId}`,
      "POST",
      null,
      auth
    );

    setOrder(data);
    setSelectedItems([]);
  };

  // ======================
  // SELECT ITEM
  // ======================

  const toggleItem = (item) => {

    const exists = selectedItems.find(i => i.id === item.id);

    if (exists) {
      setSelectedItems(
        selectedItems.filter(i => i.id !== item.id)
      );
    } else {
      setSelectedItems([...selectedItems, item]);
    }
  };

  // ======================
  // CREATE REQUEST
  // ======================

  const createRequest = async () => {

    if (selectedItems.length === 0) {
      alert("Выберите проблемные позиции");
      return;
    }

    await apiClient(
      "/api/support-requests",
      "POST",
      {
        masterId: order.masterId,
        orderId: order.orderId,
        clientId: order.clientId,
        deviceSerial: order.deviceSerial,    // ← новое
        deviceModel: order.deviceModel,      // ← новое
        description,
        items: selectedItems
      },
      auth
    );

    alert("Обращение создано");

    setOrder(null);
    setOrderId("");
    setSelectedItems([]);
    setDescription("");
  };

  // ======================
  // UI
  // ======================

  return (

    <div>

      <h2>Создание обращения поддержки</h2>

      <hr />

      <h3>Поиск заказа</h3>

      <input
        placeholder="Введите ID заказа"
        value={orderId}
        onChange={e => setOrderId(e.target.value)}
      />

      <button onClick={searchOrder}>
        Найти заказ
      </button>

      <hr />

      {order && (

        <div>

          <h3>Информация о заказе</h3>

          <p>ID заказа: {order.orderId}</p>
          <p>Клиент: {order.clientId}</p>
          <p>Мастер: {order.masterId}</p>
          <p>Статус: {order.status}</p>
          <p>Серийный номер: {order.deviceSerial || 'Не указан'}</p>      {/* ← новое */}
          <p>Модель устройства: {order.deviceModel || 'Не указана'}</p>   {/* ← новое */}
          <p>Завершено: {order.completedAt}</p>
          <p>Диагностика: {order.diagnosticResult}</p>

          <hr />

          <h3>Позиции заказа</h3>

          {order.items.length === 0 && (
            <p>В заказе нет позиций</p>
          )}

          {order.items.map(item =>

            <div key={item.id} style={{ border: "1px solid gray", margin: 5, padding: 5 }}>

              <input
                type="checkbox"
                checked={selectedItems.some(i => i.id === item.id)}
                onChange={() => toggleItem(item)}
              />

              <b>{item.name}</b>

              <p>Тип: {item.itemType}</p>
              <p>Категория: {item.category}</p>

              {item.quantity && (
                <p>Количество: {item.quantity}</p>
              )}

              <p>Цена: {item.sellPrice}</p>

            </div>

          )}

          <hr />

          <h3>Описание проблемы</h3>

          <textarea
            value={description}
            onChange={e => setDescription(e.target.value)}
            rows={4}
            cols={50}
          />

          <br />

          <button onClick={createRequest}>
            Создать обращение
          </button>

        </div>

      )}

    </div>

  );
}