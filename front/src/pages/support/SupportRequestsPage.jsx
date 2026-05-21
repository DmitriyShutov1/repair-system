import React, { useContext, useEffect, useState } from "react";
import { AuthContext } from "../../auth/AuthContext";
import { apiClient } from "../../api/apiClient";
import { useNavigate } from "react-router-dom";

const statuses = [
  "CREATED",
  "RETURN_REQUIRED",
  "RETURNED",
  "WARRANTY_REPAIR_REQUIRED",
  "WARRANTY_REPAIR_IN_PROGRESS",
  "WARRANTY_REPAIR_COMPLETED"
];

export default function SupportRequestsPage() {
  const auth = useContext(AuthContext);
  const navigate = useNavigate();

  const [requests, setRequests] = useState([]);
  const [status, setStatus] = useState("CREATED");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0); 

  const loadRequests = async (p = 0) => {
    const data = await apiClient(
      `/api/support-requests?status=${status}&page=${p}&size=10`,
      "GET",
      null,
      auth
    );

    setRequests(data.content);
    setPage(p);
    setTotalPages(data.totalPages); 
  };

  useEffect(() => {
    loadRequests(0);
  }, [status]);

  const nextPage = () => {
    if (page + 1 >= totalPages) return;
    loadRequests(page + 1);
  };

  const prevPage = () => {
    if (page === 0) return;
    loadRequests(page - 1);
  };

  return (
    <div>
      <h2>Мои обращения</h2>

      <select value={status} onChange={(e) => setStatus(e.target.value)}>
        {statuses.map((s) => (
          <option key={s}>{s}</option>
        ))}
      </select>

      <hr />

      {requests.map((r) => (
        <div key={r.id} style={{ border: "1px solid gray", padding: 10, margin: 10 }}>
          <b>Обращение #{r.id}</b>
          <p>Заказ: {r.orderId}</p>
          <p>Описание: {r.description}</p>
          <p>Создано: {r.createdAt}</p>
          <button onClick={() => navigate(`/support/requests/${r.id}`)}>
            Подробнее
          </button>
        </div>
      ))}

      {totalPages > 1 && (
        <div>
          <button onClick={prevPage} disabled={page === 0}>
            Назад
          </button>
          <span> Страница {page + 1} из {totalPages} </span>
          <button onClick={nextPage} disabled={page + 1 >= totalPages}>
            Вперёд
          </button>
        </div>
      )}
    </div>
  );
}