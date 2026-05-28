import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const ProtectedRoute = () => {
  const { isAuthenticated, loading } = useAuth();

  console.log('[AuthDebug] ProtectedRoute - Autenticado:', isAuthenticated, 'Carregando:', loading);

  // Espera o AuthProvider confirmar o cookie antes de decidir redirecionar.
  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Verificando sessão...</p>
      </div>
    );
  }

  // Outlet preserva a hierarquia de rotas protegidas; Navigate evita historico de tela proibida.
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};

export default ProtectedRoute;
