import React, { useState, useContext } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';

export default function AdminUsersPage() {

  const auth = useContext(AuthContext);

  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [filter, setFilter] = useState({
    role: 'CLIENT',
    status: 'ACTIVE'
  });

  const [createForm, setCreateForm] = useState({
    phone: '',
    email: '',
    role: 'CLIENT',
    branchId: ''
  });

  const [editUser, setEditUser] = useState(null);

  const loadUsers = async (pageNumber = 0) => {

    const data = await apiClient(
      `/api/crud/users/by-role?role=${filter.role}&status=${filter.status}&page=${pageNumber}&size=5`,
      'GET',
      null,
      auth
    );

    setUsers(data.content);
    setTotalPages(data.totalPages);
    setPage(pageNumber);
  };


  const createUser = async () => {

    const body = {
      phone: createForm.phone,
      email: createForm.email || null,
      role: createForm.role
    };

    if (createForm.branchId) {
      body.branchId = Number(createForm.branchId);
    }

    await apiClient('/api/crud/users/createByAdmin', 'POST', body, auth);

    alert('User created');
  };

  const updateUser = async () => {

    const body = {};

    if (editUser.email) body.email = editUser.email;
    if (editUser.phone) body.phone = editUser.phone;
    if (editUser.role) body.role = editUser.role;
    if (editUser.status) body.status = editUser.status;

    if (editUser.branchId !== '') {
      body.branchId = editUser.branchId ? Number(editUser.branchId) : null;
    }

    await apiClient(`/api/crud/users/${editUser.id}`, 'PATCH', body, auth);

    setEditUser(null);
    loadUsers(page);
  };

  const deleteUser = async (id) => {
    await apiClient(`/api/crud/users/${id}`, 'DELETE', null, auth);
    loadUsers(page);
  };

  return (
    <div>

      <h2>Фильтр пользователей</h2>

      <select onChange={e => setFilter({...filter, role: e.target.value})}>
        <option value="CLIENT">CLIENT</option>
        <option value="MASTER">MASTER</option>
        <option value="SUPPORT">SUPPORT</option>
        <option value="ADMIN">ADMIN</option>
      </select>

      <select onChange={e => setFilter({...filter, status: e.target.value})}>
        <option value="ACTIVE">ACTIVE</option>
        <option value="BLOCKED">BLOCKED</option>
      </select>

      <button onClick={() => loadUsers(0)}>Загрузить</button>

      <hr />

      <h2>Создать пользователя</h2>

      <input placeholder="Phone"
        onChange={e => setCreateForm({...createForm, phone: e.target.value})} />

      <input placeholder="Email"
        onChange={e => setCreateForm({...createForm, email: e.target.value})} />

      <input placeholder="Branch ID (optional)"
        onChange={e => setCreateForm({...createForm, branchId: e.target.value})} />

      <select onChange={e => setCreateForm({...createForm, role: e.target.value})}>
        <option value="CLIENT">CLIENT</option>
        <option value="MASTER">MASTER</option>
        <option value="SUPPORT">SUPPORT</option>
        <option value="ADMIN">ADMIN</option>
      </select>

      <button onClick={createUser}>Создать</button>

      <hr />

      <h2>Список</h2>

      <table border="1">
        <thead>
          <tr>
            <th>ID</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Role</th>
            <th>Status</th>
            <th>Branch</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {users.map(u => (
            <tr key={u.id}>
              <td>{u.id}</td>
              <td>{u.email}</td>
              <td>{u.phone}</td>
              <td>{u.role}</td>
              <td>{u.status}</td>
              <td>{u.branchId}</td>
              <td>
                <button onClick={() => setEditUser(u)}>Edit</button>
                <button onClick={() => deleteUser(u.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div>
        <button disabled={page === 0}
          onClick={() => loadUsers(page - 1)}>Prev</button>

        <span> Page {page + 1} / {totalPages} </span>

        <button disabled={page + 1 >= totalPages}
          onClick={() => loadUsers(page + 1)}>Next</button>
      </div>

      {editUser && (
        <div>
          <h3>Редактирование</h3>

          <input defaultValue={editUser.email}
            onChange={e => setEditUser({...editUser, email: e.target.value})} />

          <input defaultValue={editUser.phone}
            onChange={e => setEditUser({...editUser, phone: e.target.value})} />

          <input defaultValue={editUser.branchId || ''}
            onChange={e => setEditUser({...editUser, branchId: e.target.value})} />

          <select defaultValue={editUser.role}
            onChange={e => setEditUser({...editUser, role: e.target.value})}>
            <option value="CLIENT">CLIENT</option>
            <option value="MASTER">MASTER</option>
            <option value="SUPPORT">SUPPORT</option>
            <option value="ADMIN">ADMIN</option>
          </select>

          <select defaultValue={editUser.status}
            onChange={e => setEditUser({...editUser, status: e.target.value})}>
            <option value="ACTIVE">ACTIVE</option>
            <option value="BLOCKED">BLOCKED</option>
          </select>

          <button onClick={updateUser}>Сохранить</button>
        </div>
      )}

    </div>
  );
}