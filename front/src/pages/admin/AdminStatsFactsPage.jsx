import { useEffect, useState, useContext } from 'react';
import { useSearchParams } from 'react-router-dom';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';

export default function AdminStatsFactsPage() {

  const auth = useContext(AuthContext);
  const [search] = useSearchParams();

  const branchId = search.get('branchId');
  const masterId = search.get('masterId');
  const period = search.get('period') || 'MONTH';

  const [data, setData] = useState([]);

  const load = async () => {

    let url;

    if (masterId) {
      url = `/api/stats/admin/master/${masterId}/facts?period=${period}`;
    } else {
      url = `/api/stats/admin/branch/${branchId}/facts?period=${period}`;
    }

    const res = await apiClient(url, 'GET', null, auth);
    setData(res.content);
  };

  useEffect(() => {
    load();
  }, []);

  return (
    <div>
      <h2>Финансовые факты</h2>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Тип</th>
            <th>Сумма</th>
            <th>Дата</th>
          </tr>
        </thead>
        <tbody>
          {data.map(f => (
            <tr key={f.id}>
              <td>{f.id}</td>
              <td>{f.type}</td>
              <td>{f.amount}</td>
              <td>{f.eventDate}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}