import { useEffect, useState, useContext } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';

export default function AdminStatsMastersPage() {

  const { branchId } = useParams();
  const [search] = useSearchParams();
  const navigate = useNavigate();
  const auth = useContext(AuthContext);

  const period = search.get('period') || 'MONTH';

  const [data, setData] = useState([]);
  const [amounts, setAmounts] = useState({});

  const load = async () => {
    const res = await apiClient(
      `/api/stats/admin/branch/${branchId}/masters?period=${period}`,
      'GET',
      null,
      auth
    );
    setData(res.content);
  };

  useEffect(() => {
    load();
  }, []);

  const send = async (masterId, type) => {
    const amount = amounts[masterId];

    await apiClient(
      `/api/stats/admin/master/${masterId}/${type}?amount=${amount}`,
      'POST',
      null,
      auth
    );

    load();
  };

  return (
    <div>
      <h2>Мастера филиала {branchId}</h2>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Доход</th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          {data.map(m => (
            <tr key={m.masterId}>
              <td>{m.masterId}</td>
              <td>{m.totalIncome}</td>
              <td>
                <button onClick={() =>
                  navigate(`/admin/stats/facts?masterId=${m.masterId}&period=${period}`)
                }>
                  Факты
                </button>

                <input
                  placeholder="сумма"
                  onChange={e =>
                    setAmounts({ ...amounts, [m.masterId]: e.target.value })
                  }
                />

                <button onClick={() => send(m.masterId, 'bonus')}>
                  +
                </button>

                <button onClick={() => send(m.masterId, 'penalty')}>
                  -
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}