import { useState, useEffect, useCallback, useRef } from 'react';
import authService from '../services/authService';
import { AuthContext } from './AuthContext';

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const isChecking = useRef(false);

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

  useEffect(() => {
    const timer = setTimeout(() => {
      checkAuth(true);
    }, 0);
    return () => clearTimeout(timer);
  }, [checkAuth]);

  const loginGoogle = () => {
    console.log('[AuthDebug] Iniciando login Google...');
    window.location.href = authService.getGoogleLoginUrl();
  };

  const loginWithEmail = async (email, password) => {
    const data = await authService.login(email, password);
    if (data.authenticated) {
      setUser(data.user);
      setIsAuthenticated(true);
      return true;
    }
    return false;
  };

  const registerWithEmail = async (nome, email, password) => {
    const data = await authService.register(nome, email, password);
    if (data.authenticated) {
      setUser(data.user);
      setIsAuthenticated(true);
      return true;
    }
    return false;
  };

  const logout = async () => {
    console.log('[AuthDebug] Executando logout...');
    try {
      await authService.logout();
      setUser(null);
      setIsAuthenticated(false);
      setLoading(false);
      window.location.href = '/login?logout=success';
    } catch (error) {
      console.error('[AuthDebug] Erro no logout:', error);
      window.location.href = '/login';
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
