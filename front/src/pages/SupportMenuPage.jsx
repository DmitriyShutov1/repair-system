import React from "react"
import {useNavigate} from "react-router-dom"

export default function SupportMenuPage(){

const navigate = useNavigate()

return(

<div>

<h2>Панель специалиста поддержки</h2>

<button onClick={()=>navigate("/support/create-request")}>
Создать обращение
</button>

<button onClick={()=>navigate("/support/requests")}>
Мои обращения
</button>

<button onClick={()=>navigate("/support/order-history")}>
История обращений заказа
</button>

</div>

)

}