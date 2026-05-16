import { useContext } from 'react';
import { AuthContext } from '../auth/AuthContext';
import './layout.css';

export default function MainLayout({ children }) {

  const auth = useContext(AuthContext);

  return (
    <>
      <div className="navbar">
        <div>
          <strong>CRM System</strong> | {auth.user?.role}
        </div>
        <button onClick={auth.logout}>Logout</button>
      </div>

      <div className="app-container">
        {children}
      </div>
    </>
  );
}