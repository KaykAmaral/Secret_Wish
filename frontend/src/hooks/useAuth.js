import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    // Falhar cedo evita componentes usando um contexto incompleto fora do provider.
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }
  return context;
};
