import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from '../layouts/MainLayout';
import Auth from '../pages/Auth/Auth';
import Register from '../pages/Auth/Register';
import Dashboard from '../pages/Dashboard/Dashboard';
import MyGroups from '../pages/MyGroups/MyGroups';
import Profile from '../pages/Profile/Profile';
import GroupDetails from '../pages/GroupDetails/GroupDetails';
import OAuthCallback from '../pages/OAuthCallback/OAuthCallback';
import Wishlist from '../pages/Wishlist/Wishlist';
import ProtectedRoute from './ProtectedRoute';

const AppRoutes = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<MainLayout />}>
          {/* Rotas Públicas */}
          <Route path="/login" element={<Auth />} />
          <Route path="/register" element={<Register />} />
          <Route path="/oauth2/callback" element={<OAuthCallback />} />

          {/* Rotas Privadas (Protegidas Individualmente) */}
          <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/my-groups" element={<ProtectedRoute><MyGroups /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
          <Route path="/wishlist" element={<ProtectedRoute><Wishlist /></ProtectedRoute>} />
          <Route path="/groups/:groupId" element={<ProtectedRoute><GroupDetails /></ProtectedRoute>} />

          {/* Redirecionamento de Root */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Route>

        {/* Catch-all fora do Layout para evitar conflitos de renderização */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;
