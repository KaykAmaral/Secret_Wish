import { AuthProvider } from './context/AuthProvider';
import AppRoutes from './routes/AppRoutes';
import './App.css';

/**
 * Componente Raiz da Aplicação.
 * 
 * Responsável por configurar os Providers globais (como o de Autenticação)
 * e injetar a árvore de rotas principal. Manter este arquivo enxuto facilita
 * a manutenção e a adição de novos contextos globais no futuro.
 */
function App() {
  return (
    /**
     * AuthProvider: Envolve toda a aplicação para garantir que o estado de login
     * e os dados do usuário estejam disponíveis em qualquer nível da árvore de componentes.
     */
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}

export default App;

