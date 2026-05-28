import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

const OAuthCallback = () => {
  const { checkAuth, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // O callback precisa revalidar a sessao depois que o backend define o cookie JWT.
    const validateSession = async () => {
      // Pequeno delay para garantir que o cookie foi processado pelo browser
      await new Promise(resolve => setTimeout(resolve, 500));
      await checkAuth();
    };

    validateSession();
  }, [checkAuth]);

  useEffect(() => {
    // Navega somente apos o contexto refletir a autenticacao confirmada.
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
