import React,{useState,useContext,useEffect} from "react"
import {AuthContext} from "../../auth/AuthContext"
import {apiClient} from "../../api/apiClient"
import {useNavigate} from "react-router-dom"


const statuses = [
  "CREATED",
  "RETURN_REQUIRED",
  "RETURNED",
  "WARRANTY_REPAIR_REQUIRED",
  "WARRANTY_REPAIR_IN_PROGRESS",
  "WARRANTY_REPAIR_COMPLETED"
];

export default function ClientSupportRequestsPage(){

const auth = useContext(AuthContext)
const navigate = useNavigate()

const [tickets,setTickets] = useState([])
const [status,setStatus] = useState("CREATED")
const [page,setPage] = useState(0)

const loadTickets = async(p=0)=>{

const data = await apiClient(
`/api/support-requests/getTicketsToClient?status=${status}&page=${p}&size=10`,
"GET",
null,
auth
)

setTickets(data.content)
setPage(p)

}

useEffect(()=>{
loadTickets(0)
},[status])

return(

<div>

<h2>Мои обращения</h2>

<select
value={status}
onChange={e=>setStatus(e.target.value)}
>
{statuses.map(s=>
<option key={s}>{s}</option>
)}
</select>

<hr/>

{tickets.map(t=>

<div key={t.id} style={{border:"1px solid gray",padding:10,margin:10}}>

<b>Обращение #{t.id}</b>

<p>Заказ: {t.orderId}</p>

<p>Статус: {t.status}</p>

<p>Описание: {t.description}</p>

<p>Создано: {new Date(t.createdAt).toLocaleString()}</p>

<button
onClick={()=>navigate(`/support/request-view/${t.id}`)}
>
Подробнее
</button>

</div>

)}

<button onClick={()=>loadTickets(page-1)}>prev</button>
<button onClick={()=>loadTickets(page+1)}>next</button>

</div>

)

}