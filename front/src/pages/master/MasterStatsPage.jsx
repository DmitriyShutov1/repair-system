import { useEffect, useState, useContext } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';
import { PERIODS } from '../../utils/statsPeriods';

export default function MasterStatsPage() {

  const auth = useContext(AuthContext);

  const [period, setPeriod] = useState('TODAY');
  const [stats, setStats] = useState(null);

  const load = async () => {
    const data = await apiClient(
      `/api/stats/master/me?period=${period}`,
      'GET',
      null,
      auth
    );
    setStats(data);
  };

  useEffect(() => {
    load();
  }, [period]);

  return (
    <div>
      <h2>Моя статистика</h2>

      <select value={period} onChange={e => setPeriod(e.target.value)}>
        {PERIODS.map(p => (
          <option key={p.value} value={p.value}>{p.label}</option>
        ))}
      </select>

      {stats && (
        <div>
          <p>Заказы: {stats.totalOrders}</p>
          <p>Отменено: {stats.cancelledOrders}</p>
          <p>Возвраты: {stats.returnedOrders}</p>
          <p>Доход: {stats.totalIncome}</p>
        </div>
      )}
    </div>
  );
}