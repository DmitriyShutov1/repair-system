import { useEffect, useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';
import { PERIODS } from '../../utils/statsPeriods';

export default function AdminStatsBranchesPage() {

  const auth = useContext(AuthContext);
  const navigate = useNavigate();

  const [period, setPeriod] = useState('MONTH');
  const [data, setData] = useState([]);
  const [page, setPage] = useState(0);

  const load = async () => {
    const res = await apiClient(
      `/api/stats/admin/branches?period=${period}&page=${page}&size=20`,
      'GET',
      null,
      auth
    );
    setData(res.content);
  };

  useEffect(() => {
    load();
  }, [period, page]);

  return (
    <div>
      <h2>Статистика филиалов</h2>

      <select value={period} onChange={e => setPeriod(e.target.value)}>
        {PERIODS.map(p => (
          <option key={p.value} value={p.value}>{p.label}</option>
        ))}
      </select>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Доход</th>
            <th>Расход</th>
            <th>Прибыль</th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          {data.map(b => (
            <tr key={b.branchId}>
              <td>{b.branchId}</td>
              <td>{b.totalIncome}</td>
              <td>{b.totalExpenses}</td>
              <td>{b.netProfit}</td>
              <td>
                <button onClick={() =>
                  navigate(`/admin/stats/branch/${b.branchId}/masters?period=${period}`)
                }>
                  Мастера
                </button>

                <button onClick={() =>
                  navigate(`/admin/stats/facts?branchId=${b.branchId}&period=${period}`)
                }>
                  Факты
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}