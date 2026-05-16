import React,{useState,useContext} from "react"
import {AuthContext} from "../../auth/AuthContext"
import {apiClient} from "../../api/apiClient"
import {useNavigate} from "react-router-dom"

export default function MasterWarrantyPage(){

const auth = useContext(AuthContext)
const navigate = useNavigate()

const [tickets,setTickets] = useState([])
const [page,setPage] = useState(0)

const loadTickets = async(p=0)=>{

const data = await apiClient(
`/api/support-requests/getTicketsToMaster?page=${p}&size=10`,
"GET",
null,
auth
)

setTickets(data.content)
setPage(p)

}

return(

<div>

<h2>Гарантийные обращения</h2>

<button onClick={()=>loadTickets(0)}>
Загрузить
</button>

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