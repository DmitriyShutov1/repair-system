import React, { useContext, useState } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';
import { useNavigate } from 'react-router-dom';

const statuses = [
  "CREATED",
  "WAITING_FOR_PARTS",
  "WAITING_FOR_APPROVAL",
  "IN_PROGRESS",
  "COMPLETED",
  "CANCELLED_BY_CLIENT",
  "CANCELLED_BY_MASTER",
  "ISSUED"
];

export default function MasterOrdersPage(){

  const auth = useContext(AuthContext);
  const navigate = useNavigate();

  const [orders,setOrders] = useState([]);

  const [status,setStatus] = useState("CREATED");

  const [page,setPage] = useState(0);
  const [totalPages,setTotalPages] = useState(0);

  const size = 10;

  const loadOrders = async (p=0)=>{

    const data = await apiClient(
      `/api/orders/master/my-orders?status=${status}&page=${p}&size=${size}`,
      'POST',
      null,
      auth
    );

    setOrders(data.content);
    setPage(data.number);
    setTotalPages(data.totalPages);

  };

  const nextPage = ()=>{
    if(page+1>=totalPages) return;
    loadOrders(page+1);
  };

  const prevPage = ()=>{
    if(page===0) return;
    loadOrders(page-1);
  };

  return(

  <div>

  <h2>Мои заказы</h2>

  <hr/>

  <h3>Поиск по статусу</h3>

  <select
  value={status}
  onChange={e=>setStatus(e.target.value)}
  >

  {statuses.map(s=>(
    <option key={s}>{s}</option>
  ))}

  </select>

  <button onClick={()=>loadOrders(0)}>
  Найти
  </button>

  <hr/>

  {orders.map(o=>(

  <div key={o.id}
  style={{border:'1px solid gray',margin:10,padding:10}}>

  <p>ID заказа: {o.orderId}</p>

  <p>Статус: {o.status}</p>

  <p>Клиент: {o.clientId}</p>

  <p>Дата создания: {o.createdAt}</p>
  
    <button
    onClick={()=>navigate(`/master/orders/${o.orderId}`)}
    >
    Подробнее
    </button>

  </div>

  ))}

  {totalPages>1 && (

  <div>

  <button onClick={prevPage}>
  ←
  </button>

  <span>
  Page {page+1} / {totalPages}
  </span>

  <button onClick={nextPage}>
  →
  </button>

  </div>

  )}

  </div>

  );

}