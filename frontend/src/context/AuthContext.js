import { createContext } from 'react';

/**
 * Definição do Contexto de Autenticação.
 * 
 * Atua como o contrato de dados para a sessão do usuário. Este contexto é exposto
 * pelo AuthProvider e consumido via hook useAuth() em toda a aplicação.
 */
export const AuthContext = createContext();
