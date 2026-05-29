import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * Componente Wrapper para Rotas Protegidas.
 * 
 * Este componente atua como um guardião de acesso (Route Guard). Ele verifica o estado
 * de autenticação do usuário antes de renderizar o conteúdo protegido.
 * 
 * Lógica:
 * 1. Enquanto a sessão está sendo validada (loading), exibe uma tela de carregamento.
 * 2. Se autenticado, renderiza os componentes filhos (children).
 * 3. Se não autenticado, redireciona o usuário para a página de login.
 * 
 * @param {Object} props.children O componente ou página que deve ser protegida.
 */
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  // Exibe feedback visual durante a validação da sessão para evitar "flashes" de conteúdo não autorizado.
  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Verificando sessão...</p>
      </div>
    );
  }

  // Redireciona para /login caso o usuário tente acessar uma rota privada sem estar logado.
  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

export default ProtectedRoute;
