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
      // Se não for a carga inicial, mostramos o loading. 
      // Na carga inicial o estado já começa como true, evitando o erro de lint.
      if (!isInitial) {
        setLoading(true);
      }
      
      const data = await authService.getStatus();
      if (data.authenticated) {
        setUser(data.user);
        setIsAuthenticated(true);
      } else {
        setUser(null);
        setIsAuthenticated(false);
      }
    } catch (error) {
      console.error('Erro ao validar sessão:', error);
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setLoading(false);
      isChecking.current = false;
    }
  }, []);

  useEffect(() => {
    // Usamos um microtask ou timeout curto para evitar o erro de cascading render
    // embora o isInitial já ajude.
    const timer = setTimeout(() => {
      checkAuth(true);
    }, 0);
    return () => clearTimeout(timer);
  }, [checkAuth]);

  const login = () => {
    window.location.href = authService.getGoogleLoginUrl();
  };

  const logout = async () => {
    try {
      await authService.logout();
      setUser(null);
      setIsAuthenticated(false);
      window.location.href = '/login';
    } catch (error) {
      console.error('Erro ao fazer logout:', error);
    }
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, loading, login, logout, checkAuth }}>
      {children}
    </AuthContext.Provider>
  );
};
