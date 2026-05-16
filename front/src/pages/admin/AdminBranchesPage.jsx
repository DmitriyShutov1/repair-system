import React, { useState, useContext } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';

export default function AdminBranchesPage() {

  const auth = useContext(AuthContext);

  const [branches, setBranches] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [form, setForm] = useState({
    name: '',
    address: '',
    phone: ''
  });

  const [editingId, setEditingId] = useState(null);

  const loadBranches = async (pageNumber = 0) => {
    const data = await apiClient(
      `/api/crud/branches?page=${pageNumber}&size=5`,
      'GET',
      null,
      auth
    );

    setBranches(data.content);
    setTotalPages(data.totalPages);
    setPage(pageNumber);
  };

  const createBranch = async () => {
    await apiClient('/api/crud/branches', 'POST', form, auth);
    setForm({ name: '', address: '', phone: '' });
    loadBranches(page);
  };

  const startEdit = (branch) => {
    setEditingId(branch.id);
    setForm({
      name: branch.name,
      address: branch.address,
      phone: branch.phone
    });
  };

  const updateBranch = async () => {
    await apiClient(
      `/api/crud/branches/${editingId}`,
      'PUT',
      form,
      auth
    );

    setEditingId(null);
    setForm({ name: '', address: '', phone: '' });
    loadBranches(page);
  };

  const deleteBranch = async (id) => {
    await apiClient(`/api/crud/branches/${id}`, 'DELETE', null, auth);
    loadBranches(page);
  };

  return (
    <div>

      <h2>Филиалы</h2>

      <button onClick={() => loadBranches(0)}>Загрузить</button>

      <hr />

      <h3>{editingId ? 'Редактировать филиал' : 'Создать филиал'}</h3>

      <input
        placeholder="Name"
        value={form.name}
        onChange={e => setForm({ ...form, name: e.target.value })}
      />

      <input
        placeholder="Address"
        value={form.address}
        onChange={e => setForm({ ...form, address: e.target.value })}
      />

      <input
        placeholder="Phone"
        value={form.phone}
        onChange={e => setForm({ ...form, phone: e.target.value })}
      />

      {editingId ? (
        <>
          <button onClick={updateBranch}>Сохранить</button>
          <button onClick={() => {
            setEditingId(null);
            setForm({ name: '', address: '', phone: '' });
          }}>
            Отмена
          </button>
        </>
      ) : (
        <button onClick={createBranch}>Создать</button>
      )}

      <hr />

      <table border="1">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Address</th>
            <th>Phone</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {branches.map(b => (
            <tr key={b.id}>
              <td>{b.id}</td>
              <td>{b.name}</td>
              <td>{b.address}</td>
              <td>{b.phone}</td>
              <td>
                <button onClick={() => startEdit(b)}>Edit</button>
                <button onClick={() => deleteBranch(b.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div>
        <button disabled={page === 0}
          onClick={() => loadBranches(page - 1)}>Prev</button>

        <span> Page {page + 1} / {totalPages} </span>

        <button disabled={page + 1 >= totalPages}
          onClick={() => loadBranches(page + 1)}>Next</button>
      </div>

    </div>
  );
}