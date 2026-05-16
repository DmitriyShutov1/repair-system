import React,{useState,useContext} from "react"
import {AuthContext} from "../../auth/AuthContext"
import {apiClient} from "../../api/apiClient"
import {useNavigate} from "react-router-dom"

export default function SupportOrderHistoryPage(){

const auth = useContext(AuthContext)
const navigate = useNavigate()

const [orderId,setOrderId] = useState("")
const [requests,setRequests] = useState([])

const loadHistory = async()=>{

const data = await apiClient(
`/api/support-requests/listForOrder/${orderId}`,
"GET",
null,
auth
)

setRequests(data)

}

return(

<div>

<h2>История обращений по заказу</h2>

<input
placeholder="Введите ID заказа"
value={orderId}
onChange={e=>setOrderId(e.target.value)}
/>

<button onClick={loadHistory}>
Найти
</button>

<hr/>

{requests.length === 0 && (
<p>Нет обращений</p>
)}

{requests.map(r=>

<div key={r.id} style={{border:"1px solid gray",padding:10,margin:10}}>

<b>Обращение #{r.id}</b>

<p>Статус: {r.status}</p>

<p>Описание: {r.description}</p>

<p>Создано: {r.createdAt}</p>

<button
onClick={()=>navigate(`/support/request-view/${r.id}`)}
>
Подробнее
</button>

</div>

)}

</div>

)

}