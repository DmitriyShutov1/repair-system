import React, {useContext, useState} from 'react'
import {AuthContext} from '../../auth/AuthContext'
import {apiClient} from '../../api/apiClient'
import {useParams,useNavigate} from 'react-router-dom'

const partCategories = [
"SCREEN","BATTERY","MOTHERBOARD","CPU","GPU","COOLING_SYSTEM",
"KEYBOARD","RAM","SSD","MICROELEMENT"
]

const serviceCategories = [
"SOFTWARE","MAINTENANCE","SOLDERING","DIAGNOSTICS","CLEANING","UPGRADE"
]

export default function MasterOrderItemsPage(){

const auth = useContext(AuthContext)
const navigate = useNavigate()
const {orderId} = useParams()

// ===== SEARCH STATE =====

const [partQuery,setPartQuery] = useState("")
const [partCategory,setPartCategory] = useState("")
const [parts,setParts] = useState([])
const [partPage,setPartPage] = useState(0)

const [serviceQuery,setServiceQuery] = useState("")
const [serviceCategory,setServiceCategory] = useState("")
const [services,setServices] = useState([])
const [servicePage,setServicePage] = useState(0)

// ===== CART =====

const [partsCart,setPartsCart] = useState([])
const [servicesCart,setServicesCart] = useState([])

// =========================
// PART SEARCH
// =========================

const searchParts = async(page=0)=>{

let url=""

if(partQuery){
url=`/api/parts/search-with-stock?query=${partQuery}&active=true&page=${page}&size=10`
}
else if(partCategory){
url=`/api/parts/by-category-with-stock?category=${partCategory}&active=true&page=${page}&size=10`
}

if(!url) return

const data = await apiClient(url,"GET",null,auth)

setParts(data.content)
setPartPage(page)
}

// =========================
// SERVICE SEARCH
// =========================

const searchServices = async(page=0)=>{

let url=""

if(serviceQuery){
url=`/api/services/search-with-price?query=${serviceQuery}&active=true&page=${page}&size=10`
}
else if(serviceCategory){
url=`/api/services/by-category-with-price?category=${serviceCategory}&active=true&page=${page}&size=10`
}

if(!url) return

const data = await apiClient(url,"GET",null,auth)

setServices(data.content)
setServicePage(page)
}

// =========================
// ADD TO CART
// =========================

const addPartToCart = (p)=>{

const existing = partsCart.find(x=>x.id===p.id)

if(existing){
existing.quantity+=1
setPartsCart([...partsCart])
}else{
setPartsCart([...partsCart,{
...p,
quantity:1
}])
}

}

const addServiceToCart = (s)=>{

if(servicesCart.find(x=>x.id===s.id)) return

setServicesCart([...servicesCart,s])
}

// =========================
// UPDATE QUANTITY
// =========================

const changeQuantity=(id,q)=>{

setPartsCart(
partsCart.map(p=>{
if(p.id===id) return {...p,quantity:q}
return p
})
)

}

// =========================
// REMOVE
// =========================

const removePart=(id)=>{
setPartsCart(partsCart.filter(p=>p.id!==id))
}

const removeService=(id)=>{
setServicesCart(servicesCart.filter(s=>s.id!==id))
}

// =========================
// SEND TO ORDERS
// =========================

const confirmItems = async()=>{

const items=[]

// PARTS
partsCart.forEach(p=>{
items.push({
id:p.id,
itemType:"PART",
name:p.name,
serviceCode:p.articleNumber,
category:p.category,
costPrice:p.costPrice,
sellPrice:p.clientPrice,
masterPercentage:p.masterPercentage,
quantity:p.quantity
})
})

// SERVICES
servicesCart.forEach(s=>{
items.push({
id:s.id,
itemType:"SERVICE",
name:s.name,
serviceCode:s.serviceCode,
category:s.category,
costPrice:s.costPrice,
sellPrice:s.clientPrice,
masterPercentage:s.masterPercentage,
quantity:1
})
})

await apiClient(
"/api/orders/items",
"POST",
{
orderId:Number(orderId),
items
},
auth
)

alert("Позиции установлены")

navigate(`/master/orders/${orderId}`)
}

// =========================
// UI
// =========================

return(

<div>

<h2>Добавление позиций ремонта</h2>

<hr/>

<h3>Поиск запчастей</h3>

<input
placeholder="Название"
value={partQuery}
onChange={e=>setPartQuery(e.target.value)}
/>

<button onClick={()=>searchParts(0)}>
Поиск
</button>

<select
value={partCategory}
onChange={e=>{
setPartCategory(e.target.value)
searchParts(0)
}}
>
<option value="">Категория</option>
{partCategories.map(c=>
<option key={c}>{c}</option>
)}
</select>

{/* {parts.map(p=>

<div key={p.id} style={{border:'1px solid gray',margin:5,padding:5}}>

<b>{p.name}</b>

<p>Артикул: {p.articleNumber}</p>
<p>Категория: {p.category}</p>
<p>Цена: {p.clientPrice}</p>
<p>Наличие: {p.quantity}</p>

<button onClick={()=>addPartToCart(p)}>
Добавить к ремонту
</button>

</div>

)} */}

{parts.map(p =>

<div key={p.id} style={{ border: '1px solid gray', margin: 5, padding: 5 }}>

  <b>{p.name}</b>

  <p>Артикул: {p.articleNumber}</p>
  <p>Категория: {p.category}</p>
  <p>Цена: {p.clientPrice}</p>
  <p>Наличие: {p.quantity}</p>
  <p>Активна: {p.active ? "Да" : "Нет"}</p>

  <button onClick={() => addPartToCart(p)}>
    Добавить к ремонту
  </button>

</div>

)}

<button onClick={()=>searchParts(partPage-1)}>prev</button>
<button onClick={()=>searchParts(partPage+1)}>next</button>

<hr/>

<h3>Поиск услуг</h3>

<input
placeholder="Название"
value={serviceQuery}
onChange={e=>setServiceQuery(e.target.value)}
/>

<button onClick={()=>searchServices(0)}>
Поиск
</button>

<select
value={serviceCategory}
onChange={e=>{
setServiceCategory(e.target.value)
searchServices(0)
}}
>
<option value="">Категория</option>
{serviceCategories.map(c=>
<option key={c}>{c}</option>
)}
</select>

{services.map(s=>

<div key={s.id} style={{border:'1px solid gray',margin:5,padding:5}}>

<b>{s.name}</b>

<p>Код: {s.serviceCode}</p>
<p>Категория: {s.category}</p>
<p>Цена: {s.clientPrice}</p>

<button onClick={()=>addServiceToCart(s)}>
Добавить к ремонту
</button>

</div>

)}

<button onClick={()=>searchServices(servicePage-1)}>prev</button>
<button onClick={()=>searchServices(servicePage+1)}>next</button>

<hr/>

<h2>Корзина ремонта</h2>

<h3>Запчасти</h3>

{partsCart.map(p=>

<div key={p.id}>

{p.name}

<input
type="number"
value={p.quantity}
onChange={e=>changeQuantity(p.id,Number(e.target.value))}
/>

<button onClick={()=>removePart(p.id)}>
Удалить
</button>

</div>

)}

<h3>Услуги</h3>

{servicesCart.map(s=>

<div key={s.id}>

{s.name}

<button onClick={()=>removeService(s.id)}>
Удалить
</button>

</div>

)}

<hr/>

<button onClick={confirmItems}>
Подтвердить позиции ремонта
</button>

</div>

)

}