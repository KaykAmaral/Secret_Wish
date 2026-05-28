import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import Auth from '../pages/Auth/Auth';
import Register from '../pages/Auth/Register';
import Dashboard from '../pages/Dashboard/Dashboard';
import GroupDetails from '../pages/GroupDetails/GroupDetails';
import OAuthCallback from '../pages/OAuthCallback/OAuthCallback';
import ProtectedRoute from './ProtectedRoute';

const AppRoutes = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* MainLayout decide se renderiza apenas o Outlet publico ou o chrome autenticado. */}
        <Route element={<MainLayout />}>
          {/* Rotas Públicas */}
          <Route path="/login" element={<Auth />} />
          <Route path="/register" element={<Register />} />
          <Route path="/oauth2/callback" element={<OAuthCallback />} />

          {/* Rotas Privadas */}
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/groups/:groupId" element={<GroupDetails />} />
          </Route>

          {/* Redirecionamentos */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;
