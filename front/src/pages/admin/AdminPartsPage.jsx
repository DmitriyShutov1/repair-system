import React, { useContext, useState } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';

const categories = [
  'SCREEN','BATTERY','MOTHERBOARD','CPU','GPU',
  'COOLING_SYSTEM','KEYBOARD','RAM','SSD','MICROELEMENT'
];

export default function AdminPartsPage() {

  const auth = useContext(AuthContext);

  const [results, setResults] = useState([]);

  const [page,setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages,setTotalPages] = useState(0);

  const [mode,setMode] = useState(null);

  const [searchName,setSearchName] = useState('');
  const [searchCategory,setSearchCategory] = useState('');
  const [searchArticle,setSearchArticle] = useState('');
  const [searchId,setSearchId] = useState('');
  const [searchActive,setSearchActive] = useState('true');

  const [createForm,setCreateForm] = useState({
    name:'',
    articleNumber:'',
    category:'SCREEN'
  });

  const [updateForm,setUpdateForm] = useState(null);

  const applyPage = (data) => {
    setResults(data.content);
    setTotalPages(data.totalPages);
  };

  const createPart = async () => {

    const data = await apiClient(
      '/api/parts',
      'POST',
      createForm,
      auth
    );

    alert("Создано id="+data.id);
  };

  const updatePart = async () => {

    const data = await apiClient(
      `/api/parts/${updateForm.id}`,
      'PUT',
      {
        name:updateForm.name,
        category:updateForm.category,
        active:updateForm.active,
        version:updateForm.version
      },
      auth
    );

    setUpdateForm(data);
  };


const clearWaitingList = async (partId) => {

  if (!window.confirm("Очистить лист ожидания для этой запчасти?")) return;

  await apiClient(
    `/api/stock/waitingClear/${partId}`,
    'DELETE',
    null,
    auth
  );

  alert("Лист ожидания очищен");
};

  const deletePart = async (id)=>{

    if(!window.confirm("Удалить?")) return;

    await apiClient(`/api/parts/${id}`,'DELETE',null,auth);

    setResults(results.filter(p=>p.id!==id));
  };

  const searchByName = async (p=0)=>{

    setMode("name");

    const data = await apiClient(
      `/api/parts/search?query=${searchName}&page=${p}&size=${size}`,
      'GET',null,auth
    );

    setPage(p);
    applyPage(data);
  };

const findByCategory = async (p=0)=>{

  setMode("category");

  const data = await apiClient(
    `/api/parts/by-category?category=${searchCategory}&active=${searchActive}&page=${p}&size=${size}`,
    'GET',null,auth
  );

  setPage(p);
  applyPage(data);
};

  const findByArticle = async ()=>{

    const data = await apiClient(
      `/api/parts/by-article?articleNumber=${searchArticle}`,
      'GET',null,auth
    );

    setResults([data]);
    setTotalPages(0);
  };

  const findById = async ()=>{

    const data = await apiClient(
      `/api/parts/${searchId}`,
      'GET',null,auth
    );

    setResults([data]);
    setTotalPages(0);
  };

  const nextPage = ()=>{

    if(page+1 >= totalPages) return;

    if(mode==="name") searchByName(page+1);
    if(mode==="category") findByCategory(page+1);
  };

  const prevPage = ()=>{

    if(page===0) return;

    if(mode==="name") searchByName(page-1);
    if(mode==="category") findByCategory(page-1);
  };

  return (
  <div>

  <h2>Управление запчастями</h2>

  <hr/>

  <h3>Создать</h3>

  <input placeholder="Name"
    onChange={e=>setCreateForm({...createForm,name:e.target.value})}/>

  <input placeholder="Article"
    onChange={e=>setCreateForm({...createForm,articleNumber:e.target.value})}/>

  <select
    onChange={e=>setCreateForm({...createForm,category:e.target.value})}>
    {categories.map(c=><option key={c}>{c}</option>)}
  </select>

  <button onClick={createPart}>Создать</button>

  <hr/>

  <h3>Поиск</h3>

  <div>
  <input placeholder="ID"
  onChange={e=>setSearchId(e.target.value)}/>
  <button onClick={findById}>Найти</button>
  </div>

  <div>
  <input placeholder="Article"
  onChange={e=>setSearchArticle(e.target.value)}/>
  <button onClick={findByArticle}>Найти</button>
  </div>

<div>

<select onChange={e=>setSearchCategory(e.target.value)}>
<option>Category</option>
{categories.map(c=><option key={c}>{c}</option>)}
</select>

<select onChange={e=>setSearchActive(e.target.value)}>

<option value="true">Active</option>
<option value="false">Inactive</option>

</select>

<button onClick={()=>findByCategory(0)}>
По категории
</button>

</div>

  <div>
  <input placeholder="Name"
  onChange={e=>setSearchName(e.target.value)}/>
  <button onClick={()=>searchByName(0)}>По имени</button>
  </div>

  <hr/>

  <h3>Результаты</h3>

  {results.map(part=>(
  <div key={part.id} style={{border:'1px solid gray',margin:10,padding:10}}>

  <p>ID: {part.id}</p>
  <p>Name: {part.name}</p>
  <p>Article: {part.articleNumber}</p>
  <p>Category: {part.category}</p>
  <p>Active: {part.active ? 'Yes':'No'}</p>

  
  <button onClick={()=>setUpdateForm(part)}>
    Edit
  </button>

    <button onClick={()=>deletePart(part.id)}>
    Delete
    </button>

    <button onClick={()=>clearWaitingList(part.id)}>
    Очистить лист ожидания
    </button>

  </div>
  ))}

  {totalPages>1 && (
  <div>

  <button onClick={prevPage}>←</button>

  <span>
  Page {page+1} / {totalPages}
  </span>

  <button onClick={nextPage}>→</button>

  </div>
  )}

  {updateForm && (

  <div>

  <hr/>

  <h3>Редактирование</h3>

  <input value={updateForm.name}
  onChange={e=>setUpdateForm({...updateForm,name:e.target.value})}/>

  <select value={updateForm.category}
  onChange={e=>setUpdateForm({...updateForm,category:e.target.value})}>
  {categories.map(c=><option key={c}>{c}</option>)}
  </select>

  <label>
  Active
  <input type="checkbox"
  checked={updateForm.active}
  onChange={e=>setUpdateForm({...updateForm,active:e.target.checked})}/>
  </label>

  <button onClick={updatePart}>Save</button>

  </div>

  )}

  </div>
  );
}