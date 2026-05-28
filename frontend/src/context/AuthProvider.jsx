import { useState, useEffect, useCallback, useRef } from 'react';
import authService from '../services/authService';
import { AuthContext } from './AuthContext';

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const isChecking = useRef(false);

  // Evita chamadas concorrentes para /auth/status quando varias telas pedem revalidacao ao mesmo tempo.
  const checkAuth = useCallback(async (isInitial = false) => {
    if (isChecking.current) return;
    isChecking.current = true;
    
    try {
      if (!isInitial) setLoading(true);
      
      const data = await authService.getStatus();
      
      console.log('[AuthDebug] Status da sessão:', data);
      
      if (data.authenticated) {
        setUser(data.user);
        setIsAuthenticated(true);
      } else {
        setUser(null);
        setIsAuthenticated(false);
      }
    } catch (error) {
      console.error('[AuthDebug] Erro ao validar sessão:', error);
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setLoading(false);
      isChecking.current = false;
    }
  }, []);

  // A primeira validacao roda depois do mount para deixar o React finalizar a renderizacao inicial.
  useEffect(() => {
    const timer = setTimeout(() => {
      checkAuth(true);
    }, 0);
    return () => clearTimeout(timer);
  }, [checkAuth]);

  // OAuth precisa sair da SPA para iniciar o fluxo controlado pelo backend.
  const loginGoogle = () => {
    console.log('[AuthDebug] Iniciando login Google...');
    window.location.href = authService.getGoogleLoginUrl();
  };

  // Login por email atualiza o contexto imediatamente quando o backend confirma a sessao.
  const loginWithEmail = async (email, password) => {
    setLoading(true);
    try {
      const data = await authService.login(email, password);
      console.log('[AuthDebug] Login email sucesso:', data);
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

  // Cadastro bem-sucedido tambem cria sessao, entao usa o mesmo estado de usuario autenticado.
  const registerWithEmail = async (nome, email, password) => {
    setLoading(true);
    try {
      const data = await authService.register(nome, email, password);
      console.log('[AuthDebug] Registro email sucesso:', data);
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

  // O backend remove o cookie; o redirecionamento limpa qualquer estado visual remanescente.
  const logout = async () => {
    console.log('[AuthDebug] Executando logout...');
    setLoading(true);
    try {
      await authService.logout();
      setUser(null);
      setIsAuthenticated(false);
      window.location.href = '/login?logout=success';
    } catch (error) {
      console.error('[AuthDebug] Erro no logout:', error);
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
