import { useEffect } from 'react';
import { NotificationContext } from '../NotificationContext';
import { setGlobalErrorHandler } from '../api/apiClient';
import MainLayout from '../layout/MainLayout';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useContext } from 'react';
import { AuthContext } from '../auth/AuthContext';
import LoginPage from '../pages/LoginPage';
import AdminPartsPage from '../pages/admin/AdminPartsPage';

import AdminMenu from '../pages/AdminMenu';
import AdminUsersPage from '../pages/admin/AdminUsersPage';
import AdminBranchesPage from '../pages/admin/AdminBranchesPage';
import AdminServicesPage from '../pages/admin/AdminServicesPage';
import AdminPricingPolicyPage from '../pages/admin/AdminPricingPolicyPage';

import MasterOrderItemsPage from '../pages/master/MasterOrderItemsPage'
import MasterStockPage from '../pages/master/MasterStockPage';
import MasterMenu from '../pages/MasterMenu';
import MasterClientsPage from '../pages/master/MasterClientsPage';
import MasterCreateOrderPage from '../pages/master/MasterCreateOrderPage';
import MasterOrdersPage from '../pages/master/MasterOrdersPage';
import MasterOrderPage from '../pages/master/MasterOrderPage';
import MasterWarrantyPage from "../pages/master/MasterWarrantyPage"
import MasterStatsPage from '../pages/master/MasterStatsPage';

import ClientMenuPage from "../pages/ClientMenuPage"
import ClientOrdersPage from "../pages/client/ClientOrdersPage"
import ClientOrderPage from "../pages/client/ClientOrderPage"
import ClientSupportRequestsPage from "../pages/client/ClientSupportRequestsPage"

import SupportMenuPage from "../pages/SupportMenuPage"
import SupportCreateRequestPage from "../pages/support/SupportCreateRequestPage"
import SupportRequestsPage from "../pages/support/SupportRequestsPage"
import SupportRequestPage from "../pages/support/SupportRequestPage"
import SupportOrderHistoryPage from "../pages/support/SupportOrderHistoryPage"
import SupportRequestViewPage from '../pages/SupportRequestViewPage';

import AdminStatsBranchesPage from '../pages/admin/AdminStatsBranchesPage';
import AdminStatsMastersPage from '../pages/admin/AdminStatsMastersPage';
import AdminStatsFactsPage from '../pages/admin/AdminStatsFactsPage';


import MasterTestSessionsPage from '../pages/master/MasterTestSessionsPage';
import MasterTestSessionDetailsPage from '../pages/master/MasterTestSessionDetailsPage';


import MasterOrderTestingPage from '../pages/master/MasterOrderTestingPage';

function App() {

  const auth = useContext(AuthContext);

  const { showError } = useContext(NotificationContext);

  useEffect(() => {
    setGlobalErrorHandler(showError);
  }, [showError]);

  if (!auth.user) {
    return <LoginPage />;
  }

  if (auth.user.role === 'ADMIN') {
    return (
      <BrowserRouter>
        <MainLayout>
          <Routes>
            <Route path="/" element={<AdminMenu />} />
            <Route path="/admin/users" element={<AdminUsersPage />} />
            <Route path="/admin/branches" element={<AdminBranchesPage />} />
            <Route path="/admin/parts" element={<AdminPartsPage />} />
            <Route path="/admin/services" element={<AdminServicesPage />} />
            <Route path="/admin/prices" element={<AdminPricingPolicyPage />} />
            <Route path="*" element={<Navigate to="/" />} />
            <Route path="/admin/stats" element={<AdminStatsBranchesPage />} />
            <Route path="/admin/stats/branch/:branchId/masters" element={<AdminStatsMastersPage />} />
            <Route path="/admin/stats/facts" element={<AdminStatsFactsPage />} />
          </Routes>
        </MainLayout>
      </BrowserRouter>
    );
  }

  if (auth.user.role === 'MASTER') {
    return (
      <BrowserRouter>
        <MainLayout>
          <Routes>
            <Route path="/" element={<MasterMenu />} />
            <Route path="/master/clients" element={<MasterClientsPage />} />
            <Route path="/master/stock" element={<MasterStockPage />} />
            <Route path="/master/orders/create" element={<MasterCreateOrderPage />} />
            <Route path="/master/orders" element={<MasterOrdersPage />} />
            <Route path="/master/orders/:orderId" element={<MasterOrderPage />} />
            <Route path="/master/orders/:orderId/items" element={<MasterOrderItemsPage/>}/>
            <Route path="/master/warranty" element={<MasterWarrantyPage/>}/>
            <Route path="/support/request-view/:id" element={<SupportRequestViewPage/>}/>
            <Route path="/master/stats" element={<MasterStatsPage />} />
            <Route path="/master/tests" element={<MasterTestSessionsPage />} />
            <Route path="/master/tests/sessions/:sessionId" element={<MasterTestSessionDetailsPage />} />
            <Route path="/master/orders/:orderId/testing" element={<MasterOrderTestingPage />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </MainLayout>
      </BrowserRouter>
    );
  }
  if (auth.user.role === 'CLIENT') {
    return (
      <BrowserRouter>
        <MainLayout>
          <Routes>
            <Route path="/client" element={<ClientMenuPage/>}/>
            <Route path="/client/orders" element={<ClientOrdersPage/>}/>
            <Route path="/client/orders/:orderId" element={<ClientOrderPage/>}/>
            <Route path="/" element={<ClientMenuPage />} />
            <Route path="/client/support" element={<ClientSupportRequestsPage/>}/>
            <Route path="*" element={<Navigate to="/" />} />
            <Route path="/support/request-view/:id" element={<SupportRequestViewPage/>}/>
          </Routes>
        </MainLayout>
      </BrowserRouter>
    );
  }
  if (auth.user.role === 'SUPPORT') {
    return (
      <BrowserRouter>
        <MainLayout>
          <Routes>
            <Route path="/support" element={<SupportMenuPage/>}/>
            <Route path="/support/create-request" element={<SupportCreateRequestPage/>}/>
            <Route path="/" element={<SupportMenuPage />} />
            <Route path="*" element={<Navigate to="/" />} />
            <Route path="/support/requests" element={<SupportRequestsPage/>}/>
            <Route path="/support/requests/:id" element={<SupportRequestPage/>}/>
            <Route path="/support/order-history" element={<SupportOrderHistoryPage/>}/>
            <Route path="/support/request-view/:id" element={<SupportRequestViewPage/>}/>
          </Routes>
        </MainLayout>
      </BrowserRouter>
    );
  }

  return <div>Нет доступа</div>;
}

export default App;