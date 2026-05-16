// import React, { useContext, useState } from 'react';
// import { AuthContext } from '../../auth/AuthContext';
// import { apiClient } from '../../api/apiClient';

// export default function MasterStockPage(){

//   const auth = useContext(AuthContext);

//   const [articleNumber,setArticleNumber] = useState('');
//   const [quantity,setQuantity] = useState('');

//   const [parts,setParts] = useState([]);

//   const [page,setPage] = useState(0);
//   const [totalPages,setTotalPages] = useState(0);

//   const size = 10;

//   // =========================
//   // RECEIVE PARTS
//   // =========================

//   const receiveParts = async () => {

//     await apiClient(
//       `/api/stock/receive?articleNumber=${articleNumber}&quantity=${quantity}`,
//       'POST',
//       null,
//       auth
//     );

//     alert("Поставка проведена");

//     loadStock(page);
//   };

//   // =========================
//   // LOAD STOCK
//   // =========================

//   const loadStock = async (p=0) => {

//     const data = await apiClient(
//       `/api/parts/withStockAndWaiting?page=${p}&size=${size}`,
//       'GET',
//       null,
//       auth
//     );

//     setParts(data.content);
//     setPage(data.number);
//     setTotalPages(data.totalPages);
//   };

//   // =========================
//   // PAGINATION
//   // =========================

//   const nextPage = () => {
//     if(page+1>=totalPages) return;
//     loadStock(page+1);
//   };

//   const prevPage = () => {
//     if(page===0) return;
//     loadStock(page-1);
//   };

//   // =========================
//   // UI
//   // =========================

//   return(

//   <div>

//   <h2>Склад мастера</h2>

//   <hr/>

//   <h3>Принять поставку</h3>

//   <input
//   placeholder="Article number"
//   value={articleNumber}
//   onChange={e=>setArticleNumber(e.target.value)}
//   />

//   <input
//   placeholder="Количество"
//   value={quantity}
//   onChange={e=>setQuantity(e.target.value)}
//   />

//   <button onClick={receiveParts}>
//   Принять
//   </button>

//   <hr/>

//   <h3>Склад и ожидание</h3>

//   <button onClick={()=>loadStock(0)}>
//   Показать склад
//   </button>

//   {parts.map(p=>(

//   <div key={p.partId}
//   style={{border:'1px solid gray',margin:10,padding:10}}>

//   <p>ID: {p.partId}</p>
//   <p>Название: {p.name}</p>
//   <p>Артикул: {p.articleNumber}</p>
//   <p>Категория: {p.category}</p>

//   <p>На складе: {p.stockQuantity}</p>
//   <p>В ожидании: {p.waitingQuantity}</p>

//   </div>

//   ))}

//   {totalPages>1 && (

//   <div>

//   <button onClick={prevPage}>
//   ←
//   </button>

//   <span>
//   Page {page+1} / {totalPages}
//   </span>

//   <button onClick={nextPage}>
//   →
//   </button>

//   </div>

//   )}

//   </div>

//   );

// }

import React, { useContext, useState } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';

export default function MasterStockPage(){

  const auth = useContext(AuthContext);

  const [articleNumber,setArticleNumber] = useState('');
  const [quantity,setQuantity] = useState('');
  const [lostQuantity,setLostQuantity] = useState('');

  const [parts,setParts] = useState([]);

  const [page,setPage] = useState(0);
  const [totalPages,setTotalPages] = useState(0);

  const size = 10;

  // =========================
  // RECEIVE PARTS
  // =========================

  const receiveParts = async () => {

    await apiClient(
      `/api/stock/receive?articleNumber=${articleNumber}&quantity=${quantity}`,
      'POST',
      null,
      auth
    );

    alert("Поставка проведена");

    loadStock(page);
  };

  // =========================
  // LOST PARTS
  // =========================

  const lostParts = async () => {

    await apiClient(
      `/api/stock/lost?articleNumber=${articleNumber}&removeIt=${lostQuantity}`,
      'POST',
      null,
      auth
    );

    alert("Списание проведено");

    loadStock(page);
  };

  // =========================
  // LOAD STOCK LIST
  // =========================

  const loadStock = async (p=0) => {

    const data = await apiClient(
      `/api/parts/withStockAndWaiting?page=${p}&size=${size}`,
      'GET',
      null,
      auth
    );

    setParts(data.content);
    setPage(data.number);
    setTotalPages(data.totalPages);
  };

  // =========================
  // SEARCH ONE PART
  // =========================

  const findByArticle = async () => {

    const data = await apiClient(
      `/api/parts/withStockAndWaitingByArticle?articleNumber=${articleNumber}`,
      'GET',
      null,
      auth
    );

    setParts([data]);
    setTotalPages(0);
  };

  // =========================
  // PAGINATION
  // =========================

  const nextPage = () => {
    if(page+1>=totalPages) return;
    loadStock(page+1);
  };

  const prevPage = () => {
    if(page===0) return;
    loadStock(page-1);
  };

  // =========================
  // UI
  // =========================

  return(

  <div>

  <h2>Склад мастера</h2>

  <hr/>

  <h3>Операции со складом</h3>

  <input
  placeholder="Article number"
  value={articleNumber}
  onChange={e=>setArticleNumber(e.target.value)}
  />

  <br/><br/>

  <input
  placeholder="Количество принять"
  value={quantity}
  onChange={e=>setQuantity(e.target.value)}
  />

  <button onClick={receiveParts}>
  Принять
  </button>

  <br/><br/>

  <input
  placeholder="Утеряно"
  value={lostQuantity}
  onChange={e=>setLostQuantity(e.target.value)}
  />

  <button onClick={lostParts}>
  Списать
  </button>

  <hr/>

  <h3>Поиск и просмотр склада</h3>

  <button onClick={()=>loadStock(0)}>
  Показать склад
  </button>

  <button onClick={findByArticle}>
  Найти по артикулу
  </button>

  {parts.map(p=>(

  <div key={p.partId}
  style={{border:'1px solid gray',margin:10,padding:10}}>

  <p>ID: {p.partId}</p>
  <p>Название: {p.name}</p>
  <p>Артикул: {p.articleNumber}</p>
  <p>Категория: {p.category}</p>

  <p>На складе: {p.stockQuantity}</p>
  <p>В ожидании: {p.waitingQuantity}</p>

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