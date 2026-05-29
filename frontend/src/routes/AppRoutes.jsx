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

/**
 * Definição da Árvore de Rotas da Aplicação.
 * 
 * Utiliza o React Router para gerenciar a navegação Single Page Application (SPA).
 * Centraliza a organização de rotas públicas e privadas, além de envolver as páginas
 * no layout principal (MainLayout).
 */
const AppRoutes = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* MainLayout envolve a maioria das rotas para prover Header e estrutura comum */}
        <Route element={<MainLayout />}>
          
          {/* --- Rotas Públicas --- */}
          {/* Telas acessíveis sem necessidade de login */}
          <Route path="/login" element={<Auth />} />
          <Route path="/register" element={<Register />} />
          <Route path="/oauth2/callback" element={<OAuthCallback />} />

          {/* --- Rotas Privadas --- */}
          {/* Cada uma destas rotas é envolvida pelo ProtectedRoute para garantir que apenas usuários logados as vejam */}
          <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/my-groups" element={<ProtectedRoute><MyGroups /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
          <Route path="/wishlist" element={<ProtectedRoute><Wishlist /></ProtectedRoute>} />
          <Route path="/groups/:groupId" element={<ProtectedRoute><GroupDetails /></ProtectedRoute>} />

          {/* Redirecionamento da rota raiz para o Dashboard */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Route>

        {/* Catch-all: Qualquer rota não definida redireciona para o login */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;
