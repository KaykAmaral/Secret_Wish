import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

/**
 * Página de Callback do OAuth2.
 * 
 * Atua como uma tela intermediária (loading) após o redirecionamento bem-sucedido
 * do Google. Sua função é revalidar a sessão junto ao backend para confirmar
 * que o cookie de autenticação foi corretamente definido no navegador.
 */
const OAuthCallback = () => {
  const { checkAuth, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    /**
     * Valida a sessão recém-criada pelo fluxo OAuth.
     */
    const validateSession = async () => {
      // Pequeno delay preventivo para garantir que o browser processou os cookies do redirecionamento.
      await new Promise(resolve => setTimeout(resolve, 500));
      await checkAuth();
    };

    validateSession();
  }, [checkAuth]);

  useEffect(() => {
    // Redireciona para o dashboard assim que o contexto global confirmar a autenticação.
    if (isAuthenticated) {
      navigate('/dashboard');
    }
  }, [isAuthenticated, navigate]);

  return (
    <div className="loading-container">
      <div className="spinner"></div>
      <p>Finalizando autenticação...</p>
    </div>
  );
};

export default OAuthCallback;
