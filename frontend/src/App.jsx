import { AuthProvider } from './context/AuthProvider';
import AppRoutes from './routes/AppRoutes';
import './App.css';

function App() {
  return (
    // AuthProvider deixa o estado de sessao disponivel para rotas, layout e telas protegidas.
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}

export default App;
