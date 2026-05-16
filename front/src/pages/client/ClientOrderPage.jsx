import React,{useState,useEffect,useContext} from "react"
import {useParams} from "react-router-dom"
import {AuthContext} from "../../auth/AuthContext"
import {apiClient} from "../../api/apiClient"

export default function ClientOrderPage(){

const {orderId} = useParams()
const auth = useContext(AuthContext)

const [pickupCode, setPickupCode] = useState(null);
const [order,setOrder] = useState(null)

const loadOrder = async()=>{

const data = await apiClient(
`/api/orders/info?orderId=${orderId}`,
"POST",
null,
auth
)

setOrder(data)

}

useEffect(()=>{
loadOrder()
},[])

const fetchPickupCode = async () => {
  if (pickupCode) return;

  const code = await apiClient(
    `/api/orders/pickup-code?orderId=${orderId}`,
    "POST",
    null,
    auth
  );

  setPickupCode(code.pickupCode); // <-- сохраняем строку
};



const confirmOrder = async()=>{

await apiClient(
`/api/orders/${orderId}/confirm`,
"POST",
null,
auth
)

alert("Заказ подтвержден")

loadOrder()

}

if(!order) return <div>Loading...</div>

return(

<div>

<h2>Заказ #{order.orderId}</h2>

<p>Статус: {order.status}</p>

<p>Мастер: {order.masterId}</p>

<p>Создан: {order.createdAt}</p>

{order.completedAt && (
<p>Завершен: {order.completedAt}</p>
)}

<hr/>

<h3>Диагностика</h3>

<p>{order.diagnosticResult}</p>

<hr/>

<h3>Позиции ремонта</h3>

{(!order.items || order.items.length===0) && (
<p>Позиции не установлены</p>
)}

{order.items?.map(i=>

<div key={i.itemId} style={{border:"1px solid gray",margin:5,padding:5}}>

<b>{i.name}</b>

<p>Категория: {i.category}</p>

{i.quantity && (
<p>Количество: {i.quantity}</p>
)}

<p>Цена: {i.sellPrice}</p>

</div>

)}

<hr/>

{order.status==="WAITING_FOR_APPROVAL" && (

<button onClick={confirmOrder}>
ПОДТВЕРДИТЬ РЕМОНТ
</button>

)}


{/* Кнопка получения кода */}
      <button onClick={fetchPickupCode}>
        {pickupCode ? "ПОКАЗАТЬ КОД ВЫДАЧИ" : "ПОЛУЧИТЬ КОД ВЫДАЧИ"}
      </button>

      {pickupCode && (
        <div style={{ marginTop: 10, padding: 10, background: "#eee" }}>
          <strong>Код выдачи:</strong> {pickupCode}
        </div>
      )}
</div>

)

}