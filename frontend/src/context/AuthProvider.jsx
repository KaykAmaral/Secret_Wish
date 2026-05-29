import { useState, useEffect, useCallback, useRef } from 'react';
import authService from '../services/authService';
import { AuthContext } from './AuthContext';

/**
 * Provedor de Autenticação (AuthProvider).
 * 
 * Este componente é o "cérebro" da sessão no frontend. Ele gerencia o estado do usuário,
 * valida a autenticação junto ao backend e fornece métodos de login/logout para a aplicação.
 * 
 * @param {Object} props.children Elementos filhos que terão acesso ao contexto.
 */
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  
  // useRef é usado para evitar loops de checagem quando múltiplos componentes montam simultaneamente.
  const isChecking = useRef(false);

  /**
   * Verifica o status da sessão no servidor.
   * 
   * @param {boolean} isInitial Indica se é a primeira checagem após o carregamento da página.
   */
  const checkAuth = useCallback(async (isInitial = false) => {
    if (isChecking.current) return;
    isChecking.current = true;
    
    try {
      // Evita "piscar" a tela de loading em revalidações silenciosas de fundo.
      if (!isInitial) setLoading(true);
      
      const data = await authService.getStatus();
      
      if (data.authenticated) {
        setUser(data.user);
        setIsAuthenticated(true);
      } else {
        setUser(null);
        setIsAuthenticated(false);
      }
    } catch (error) {
      console.error('[AuthContext] Falha na validação da sessão:', error);
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setLoading(false);
      isChecking.current = false;
    }
  }, []);

  // Realiza a checagem automática ao montar o provider (entrada no app).
  useEffect(() => {
    const timer = setTimeout(() => {
      checkAuth(true);
    }, 0);
    return () => clearTimeout(timer);
  }, [checkAuth]);

  /**
   * Inicia o fluxo de login via Google.
   * Redireciona a janela atual para o endpoint de autorização do backend.
   */
  const loginGoogle = () => {
    window.location.href = authService.getGoogleLoginUrl();
  };

  /**
   * Executa login via credenciais locais (e-mail/senha).
   */
  const loginWithEmail = async (email, password) => {
    setLoading(true);
    try {
      const data = await authService.login(email, password);
      if (data.authenticated) {
        setUser(data.user);
        setIsAuthenticated(true);
        return true;
      }
      return false;
    } finally {
      setLoading(false);
    }
  };

  /**
   * Cadastra um novo usuário e já estabelece a sessão.
   */
  const registerWithEmail = async (nome, email, password) => {
    setLoading(true);
    try {
      const data = await authService.register(nome, email, password);
      if (data.authenticated) {
        setUser(data.user);
        setIsAuthenticated(true);
        return true;
      }
      return false;
    } finally {
      setLoading(false);
    }
  };

  /**
   * Encerra a sessão e limpa os estados locais.
   * Redireciona para a tela de login após a limpeza.
   */
  const logout = async () => {
    setLoading(true);
    try {
      await authService.logout();
      setUser(null);
      setIsAuthenticated(false);
      // O query param 'logout=success' pode ser usado pela tela de login para exibir um feedback.
      window.location.href = '/login?logout=success';
    } catch (error) {
      console.error('[AuthContext] Erro ao deslogar:', error);
      window.location.href = '/login';
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      isAuthenticated, 
      loading, 
      login: loginGoogle, 
      loginWithEmail,
      registerWithEmail,
      logout, 
      checkAuth 
    }}>
      {children}
    </AuthContext.Provider>
  );
};
