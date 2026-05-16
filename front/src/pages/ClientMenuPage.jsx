import React from "react"
import {useNavigate} from "react-router-dom"

export default function ClientMenuPage(){

const navigate = useNavigate()

return(

<div>

<h2>Кабинет клиента</h2>

<button onClick={()=>navigate("/client/orders")}>
Мои заказы
</button>

<button onClick={()=>navigate("/client/support")}>
Мои обращения
</button>

</div>

)

}