// import React, { useContext, useState } from 'react';
// import { AuthContext } from '../../auth/AuthContext';
// import { apiClient } from '../../api/apiClient';

// export default function MasterCreateOrderPage(){

//   const auth = useContext(AuthContext);

//   const [clientId,setClientId] = useState('');
//   const [warrantyId,setWarrantyId] = useState('');
//   const [diagnosticResult,setDiagnosticResult] = useState('');

//   const createOrder = async () => {

//     const data = await apiClient(
//       '/api/orders',
//       'POST',
//       {
//         clientId: clientId || null,
//         warrantyId: warrantyId || null,
//         diagnosticResult
//       },
//       auth
//     );

//     alert("Заказ создан ID = " + data.id);
//   };

//   return(

//   <div>

//   <h2>Создание заказа</h2>

//   <hr/>

//   <div>

//   <p>ID клиента</p>

//   <input
//   value={clientId}
//   onChange={e=>setClientId(e.target.value)}
//   />

//   </div>

//   <div>

//   <p>ID гарантии (если есть)</p>

//   <input
//   value={warrantyId}
//   onChange={e=>setWarrantyId(e.target.value)}
//   />

//   </div>

//   <div>

//   <p>Результат диагностики</p>

//   <textarea
//   value={diagnosticResult}
//   onChange={e=>setDiagnosticResult(e.target.value)}
//   />

//   </div>

//   <br/>

//   <button onClick={createOrder}>
//   Создать заказ
//   </button>

//   </div>

//   );

// }

import React, { useContext, useState } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';

export default function MasterCreateOrderPage() {

  const auth = useContext(AuthContext);

  const [clientId, setClientId] = useState('');
  const [warrantyId, setWarrantyId] = useState('');
  const [deviceSerial, setDeviceSerial] = useState('');   // ← новое
  const [deviceModel, setDeviceModel] = useState('');     // ← новое
  const [diagnosticResult, setDiagnosticResult] = useState('');

  const createOrder = async () => {

    const data = await apiClient(
      '/api/orders',
      'POST',
      {
        clientId: clientId || null,
        warrantyId: warrantyId || null,
        deviceSerial: deviceSerial || null,    // ← новое
        deviceModel: deviceModel || null,      // ← новое
        diagnosticResult
      },
      auth
    );

    alert("Заказ создан ID = " + data.orderId);
  };

  return (

    <div>

      <h2>Создание заказа</h2>

      <hr />

      <div>
        <p>ID клиента</p>
        <input
          value={clientId}
          onChange={e => setClientId(e.target.value)}
        />
      </div>

      <div>
        <p>Серийный номер устройства</p>          {/* ← новый блок */}
        <input
          value={deviceSerial}
          onChange={e => setDeviceSerial(e.target.value)}
        />
      </div>

      <div>
        <p>Модель ноутбука</p>                    {/* ← новый блок */}
        <input
          value={deviceModel}
          onChange={e => setDeviceModel(e.target.value)}
        />
      </div>

      <div>
        <p>ID гарантии (если есть)</p>
        <input
          value={warrantyId}
          onChange={e => setWarrantyId(e.target.value)}
        />
      </div>

      <div>
        <p>Результат диагностики</p>
        <textarea
          value={diagnosticResult}
          onChange={e => setDiagnosticResult(e.target.value)}
        />
      </div>

      <br />

      <button onClick={createOrder}>
        Создать заказ
      </button>

    </div>

  );
}