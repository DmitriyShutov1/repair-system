import React, { useContext, useState } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';

const categories = [
  'SOFTWARE', 'MAINTENANCE', 'SOLDERING',
  'DIAGNOSTICS', 'CLEANING', 'UPGRADE'
];

export default function AdminServicesPage() {

  const auth = useContext(AuthContext);

  const [results, setResults] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [mode, setMode] = useState(null);

  const [searchName, setSearchName] = useState('');
  const [searchCategory, setSearchCategory] = useState('');
  const [searchCode, setSearchCode] = useState('');
  const [searchId, setSearchId] = useState('');

  const [createForm, setCreateForm] = useState({
    name: '',
    serviceCode: '',
    category: 'SOFTWARE'
  });

  const [updateForm, setUpdateForm] = useState(null);

  const applyPage = (data) => {
    setResults(data.content || []);
    setTotalPages(data.totalPages || 0);
  };

  const createService = async () => {
    const data = await apiClient('/api/services', 'POST', createForm, auth);
    alert("Создано id=" + data.id);
    setCreateForm({ name: '', serviceCode: '', category: 'SOFTWARE' });
    if (mode === 'category') findByCategory(page);
    else if (mode === 'name') searchByName(page);
  };

  const updateService = async () => {
    const data = await apiClient(
      `/api/services/${updateForm.id}`,
      'PUT',
      {
        name: updateForm.name,
        category: updateForm.category,
        active: updateForm.active,
        version: updateForm.version
      },
      auth
    );
    setResults(prev => prev.map(s => s.id === data.id ? data : s));
    setUpdateForm(null);
  };

  const deleteService = async (id) => {
    if (!window.confirm('Удалить услугу?')) return;
    await apiClient(`/api/services/${id}`, 'DELETE', null, auth);
    setResults(prev => prev.filter(s => s.id !== id));
  };

  const searchByName = async (p = 0) => {
    setMode("name");
    const data = await apiClient(
      `/api/services/search?query=${encodeURIComponent(searchName)}&page=${p}&size=${size}`,
      'GET', null, auth
    );
    setPage(p);
    applyPage(data);
  };

  const findByCategory = async (p = 0) => {
    setMode("category");
    const data = await apiClient(
      `/api/services/by-category?category=${searchCategory}&active=true&page=${p}&size=${size}`,
      'GET', null, auth
    );
    setPage(p);
    applyPage(data);
  };

  const findByCode = async () => {
    const data = await apiClient(
      `/api/services/by-code?serviceCode=${encodeURIComponent(searchCode)}`,
      'GET', null, auth
    );
    setResults([data]);
    setTotalPages(0);
  };

  const findById = async () => {
    const data = await apiClient(
      `/api/services/${searchId}`,
      'GET', null, auth
    );
    setResults([data]);
    setTotalPages(0);
  };

  const nextPage = () => {
    if (page + 1 >= totalPages) return;
    if (mode === "name") searchByName(page + 1);
    if (mode === "category") findByCategory(page + 1);
  };

  const prevPage = () => {
    if (page === 0) return;
    if (mode === "name") searchByName(page - 1);
    if (mode === "category") findByCategory(page - 1);
  };

  return (
    <div>
      <h2>Управление услугами</h2>
      <hr />

      <h3>Создать услугу</h3>
      <input
        placeholder="Название"
        value={createForm.name}
        onChange={e => setCreateForm({ ...createForm, name: e.target.value })}
      />
      <input
        placeholder="Код услуги"
        value={createForm.serviceCode}
        onChange={e => setCreateForm({ ...createForm, serviceCode: e.target.value })}
      />
      <select
        value={createForm.category}
        onChange={e => setCreateForm({ ...createForm, category: e.target.value })}
      >
        {categories.map(c => <option key={c} value={c}>{c}</option>)}
      </select>
      <button onClick={createService}>Создать</button>

      <hr />

      <h3>Поиск</h3>
      <div style={{ marginBottom: 5 }}>
        <input placeholder="ID" value={searchId} onChange={e => setSearchId(e.target.value)} />
        <button onClick={findById}>Найти по ID</button>
      </div>
      <div style={{ marginBottom: 5 }}>
        <input placeholder="Код услуги" value={searchCode} onChange={e => setSearchCode(e.target.value)} />
        <button onClick={findByCode}>Найти по коду</button>
      </div>
      <div style={{ marginBottom: 5 }}>
        <select value={searchCategory} onChange={e => setSearchCategory(e.target.value)}>
          <option value="">-- Выберите категорию --</option>
          {categories.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
        <button onClick={() => findByCategory(0)} disabled={!searchCategory}>
          По категории
        </button>
      </div>
      <div style={{ marginBottom: 5 }}>
        <input placeholder="Название" value={searchName} onChange={e => setSearchName(e.target.value)} />
        <button onClick={() => searchByName(0)}>По названию</button>
      </div>

      <hr />

      {updateForm && (
        <div style={{ border: '2px solid blue', padding: 10, marginBottom: 15, background: '#f0f4ff' }}>
          <h4>Редактирование: {updateForm.name} (ID: {updateForm.id})</h4>
          <label>Название: </label>
          <input
            value={updateForm.name}
            onChange={e => setUpdateForm({ ...updateForm, name: e.target.value })}
          />
          <br />
          <label>Категория: </label>
          <select
            value={updateForm.category}
            onChange={e => setUpdateForm({ ...updateForm, category: e.target.value })}
          >
            {categories.map(c => <option key={c} value={c}>{c}</option>)}
          </select>
          <br />
          <label>
            <input
              type="checkbox"
              checked={updateForm.active}
              onChange={e => setUpdateForm({ ...updateForm, active: e.target.checked })}
            />
            Активна
          </label>
          <br />
          <button onClick={updateService} style={{ marginRight: 10 }}>Сохранить</button>
          <button onClick={() => setUpdateForm(null)}>Отмена</button>
        </div>
      )}

      {results.length === 0 && <p>Ничего не найдено</p>}
      {results.map(service => (
        <div key={service.id} style={{ border: '1px solid gray', margin: 10, padding: 10 }}>
          <p><b>ID:</b> {service.id}</p>
          <p><b>Название:</b> {service.name}</p>
          <p><b>Код:</b> {service.serviceCode}</p>
          <p><b>Категория:</b> {service.category}</p>
          <p><b>Активна:</b> {service.active ? 'Да' : 'Нет'}</p>
          <button onClick={() => setUpdateForm(service)}>Редактировать</button>
          <button onClick={() => deleteService(service.id)} style={{ marginLeft: 5 }}>Удалить</button>
        </div>
      ))}

      {totalPages > 1 && (
        <div style={{ marginTop: 10 }}>
          <button onClick={prevPage} disabled={page === 0}>← Назад</button>
          <span style={{ margin: '0 10px' }}>Стр. {page + 1} / {totalPages}</span>
          <button onClick={nextPage} disabled={page + 1 >= totalPages}>Вперёд →</button>
        </div>
      )}
    </div>
  );
}