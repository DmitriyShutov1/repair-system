import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from './../auth/AuthContext';
import { apiClient } from './../api/apiClient';
import { useParams, useNavigate } from 'react-router-dom';

export default function SupportRequestViewPage(){

const {id} = useParams()
const auth = useContext(AuthContext)

const [request,setRequest] = useState(null)

const loadRequest = async()=>{

const data = await apiClient(
`/api/support-requests/${id}/info`,
"GET",
null,
auth
)

setRequest(data)

}

useEffect(()=>{
loadRequest()
},[])

if(!request) return <div>Loading...</div>

return(

<div>

<h2>Обращение #{request.id}</h2>

<p>Заказ: {request.orderId}</p>

<p>Мастер: {request.masterId}</p>

<p>Клиент: {request.clientId}</p>

<p>Статус: {request.status}</p>

<p>Филиал: {request.branchId}</p>

<p>Описание: {request.description}</p>

<p>Серийный номер: {request.deviceSerial || 'Не указан'}</p>      

<p>Модель устройства: {request.deviceModel || 'Не указана'}</p>   

<p>Создано: {request.createdAt}</p>

{request.completedAt && (
<p>Завершено: {request.completedAt}</p>
)}

<hr/>

<h3>Проблемные позиции</h3>

{request.items.map(i=>

<div key={i.id} style={{border:"1px solid gray",margin:5,padding:5}}>

<b>{i.name}</b>

<p>Тип: {i.itemType}</p>

<p>Категория: {i.category}</p>

<p>Количество: {i.quantity}</p>

<p>Цена: {i.sellPrice}</p>

<p>Добавлено: {i.createdAt}</p>

</div>

)}

</div>

)

}