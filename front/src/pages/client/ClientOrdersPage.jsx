// import React,{useState,useContext,useEffect} from "react"
// import {AuthContext} from "../../auth/AuthContext"
// import {apiClient} from "../../api/apiClient"
// import {useNavigate} from "react-router-dom"

// export default function ClientOrdersPage(){

// const auth = useContext(AuthContext)
// const navigate = useNavigate()

// const [orders,setOrders] = useState([])
// const [page,setPage] = useState(0)

// const [filter,setFilter] = useState("ACTIVE")

// const loadOrders = async(p=0)=>{

// let url=""

// if(filter==="ACTIVE"){
// url=`/api/orders/client/my-active-orders?page=${p}&size=10`
// }else{
// url=`/api/orders/client/my-issued-orders?page=${p}&size=10`
// }

// const data = await apiClient(url,"POST",null,auth)

// setOrders(data.content)
// setPage(p)

// }

// useEffect(()=>{
// loadOrders(0)
// },[filter])

// return(

// <div>

// <h2>Мои заказы</h2>

// <select
// value={filter}
// onChange={e=>setFilter(e.target.value)}
// >

// <option value="ACTIVE">Активные</option>
// <option value="ISSUED">Выданные</option>

// </select>

// <hr/>

// {orders.map(o=>

// <div key={o.orderId} style={{border:"1px solid gray",margin:10,padding:10}}>

// <b>Заказ #{o.orderId}</b>

// <p>Статус: {o.status}</p>

// {o.status==="WAITING_FOR_APPROVAL" && (
// <p style={{color:"red",fontWeight:"bold"}}>
// ТРЕБУЕТСЯ ПОДТВЕРЖДЕНИЕ
// </p>
// )}

// <p>Создан: {o.createdAt}</p>

// <button
// onClick={()=>navigate(`/client/orders/${o.orderId}`)}
// >
// Подробнее
// </button>

// </div>

// )}

// <button onClick={()=>loadOrders(page-1)}>
// prev
// </button>

// <button onClick={()=>loadOrders(page+1)}>
// next
// </button>

// </div>

// )

// }

import React, { useState, useContext, useEffect } from "react";
import { AuthContext } from "../../auth/AuthContext";
import { apiClient } from "../../api/apiClient";
import { useNavigate } from "react-router-dom";

export default function ClientOrdersPage() {

  const auth = useContext(AuthContext);
  const navigate = useNavigate();

  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [filter, setFilter] = useState("ACTIVE");

  const loadOrders = async (p = 0) => {
    let url = "";
    if (filter === "ACTIVE") {
      url = `/api/orders/client/my-active-orders?page=${p}&size=10`;
    } else {
      url = `/api/orders/client/my-issued-orders?page=${p}&size=10`;
    }
    const data = await apiClient(url, "POST", null, auth);
    setOrders(data.content);
    setPage(p);
  };

  useEffect(() => {
    loadOrders(0);
  }, [filter]);

  const formatDate = (raw) => {
    if (!raw) return "";
    return raw.split("T")[0];
  };

  return (
    <div>
      <h2>Мои заказы</h2>

      <select value={filter} onChange={e => setFilter(e.target.value)}>
        <option value="ACTIVE">Активные</option>
        <option value="ISSUED">Выданные</option>
      </select>

      <hr />

      {orders.map(o =>
        <div key={o.orderId} style={{ border: "1px solid gray", margin: 10, padding: 10 }}>
          <b>Заказ #{o.orderId}</b>
          <p>Модель: {o.deviceModel || "Не указана"}</p>
          <p>Статус: {o.status}</p>

          {o.status === "WAITING_FOR_APPROVAL" && (
            <p style={{ color: "red", fontWeight: "bold" }}>
              ТРЕБУЕТСЯ ПОДТВЕРЖДЕНИЕ
            </p>
          )}

          <p>Создан: {formatDate(o.createdAt)}</p>

          <button onClick={() => navigate(`/client/orders/${o.orderId}`)}>
            Подробнее
          </button>
        </div>
      )}

      <button onClick={() => loadOrders(page - 1)}>prev</button>
      <button onClick={() => loadOrders(page + 1)}>next</button>
    </div>
  );
}