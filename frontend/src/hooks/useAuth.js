import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

/**
 * Custom Hook: useAuth
 * 
 * Facilita o consumo do contexto de autenticação em qualquer componente funcional.
 * Garante que o desenvolvedor receba um erro claro caso tente usar o hook fora do AuthProvider.
 * 
 * @returns {Object} Dados e funções de autenticação (user, login, logout, etc).
 * @throws {Error} Se usado fora de um AuthProvider.
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  
  if (!context) {
    throw new Error('useAuth deve ser utilizado dentro de um <AuthProvider />');
  }
  
  return context;
};
