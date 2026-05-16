import React, { useState, useContext } from 'react';
import { AuthContext } from '../../auth/AuthContext';
import { apiClient } from '../../api/apiClient';

export default function MasterClientsPage() {

  const auth = useContext(AuthContext);

  const [createForm, setCreateForm] = useState({
    login: '',
    password: '',
    email: '',
    phone: ''
  });

  const [searchEmail, setSearchEmail] = useState('');
  const [searchID, setSearchID] = useState('');
  const [searchPhone, setSearchPhone] = useState('');
  const [foundUser, setFoundUser] = useState(null);

  const createClient = async () => {
    const data = await apiClient(
      '/api/crud/users/createByMaster',
      'POST',
      createForm,
      auth
    );

    setFoundUser(data);
  };

  const findByEmail = async () => {
    const data = await apiClient(
      `/api/crud/users/by-email?email=${searchEmail}`,
      'GET',
      null,
      auth
    );

    setFoundUser(data);
  };

  const findByPhone = async () => {
    const data = await apiClient(
      `/api/crud/users/by-phone?phone=${searchPhone}`,
      'GET',
      null,
      auth
    );

    setFoundUser(data);
  };

  const findByID = async () => {
    const data = await apiClient(
      `/api/crud/users/${searchID}`,
      'GET',
      null,
      auth
    );

    setFoundUser(data);
  };

  return (
    <div>

      <h2>Управление клиентами</h2>

      <hr />

      <h3>Создать клиента</h3>

      <input
        placeholder="Login"
        onChange={e => setCreateForm({ ...createForm, login: e.target.value })}
      />

      <input
        placeholder="Password"
        onChange={e => setCreateForm({ ...createForm, password: e.target.value })}
      />

      <input
        placeholder="Email"
        onChange={e => setCreateForm({ ...createForm, email: e.target.value })}
      />

      <input
        placeholder="Phone"
        onChange={e => setCreateForm({ ...createForm, phone: e.target.value })}
      />

      <button onClick={createClient}>Создать клиента</button>

      <hr />

      <h3>Поиск клиента</h3>

      <div>
        <input
          placeholder="Поиск по email"
          onChange={e => setSearchEmail(e.target.value)}
        />
        <button onClick={findByEmail}>Найти</button>
      </div>

      <div>
        <input
          placeholder="Поиск по телефону"
          onChange={e => setSearchPhone(e.target.value)}
        />
        <button onClick={findByPhone}>Найти</button>
      </div>

      <div>
        <input
          placeholder="Поиск по ID"
          onChange={e => setSearchID(e.target.value)}
        />
        <button onClick={findByID}>Найти</button>
      </div>

      <hr />

      {foundUser && (
        <div>
          <h3>Результат</h3>
          <p>ID: {foundUser.id}</p>
          <p>Email: {foundUser.email}</p>
          <p>Phone: {foundUser.phone}</p>
          <p>Role: {foundUser.role}</p>
          <p>Status: {foundUser.status}</p>
        </div>
      )}

    </div>
  );
}