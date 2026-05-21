import { useAuth } from '../../hooks/useAuth';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout } = useAuth();

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div className="header-content">
          <div className="logo">🎁 Secret Wish</div>
          <div className="user-nav">
            <span className="user-name">{user?.nome}</span>
            <button className="logout-btn" onClick={logout}>Sair</button>
          </div>
        </div>
      </header>

      <main className="dashboard-main">
        <div className="welcome-card">
          <h1>Bem-vindo, {user?.nome}!</h1>
          <p>Você está autenticado com o e-mail: <strong>{user?.email}</strong></p>
          <div className="status-badge">Sessão Ativa via JWT</div>
        </div>

        <div className="next-steps-grid">
          <div className="step-card">
            <h3>Meus Grupos</h3>
            <p>Gerencie seus grupos de amigo secreto.</p>
            <button className="secondary-btn" disabled>Em breve</button>
          </div>
          <div className="step-card">
            <h3>Minha Wishlist</h3>
            <p>Adicione presentes que você gostaria de ganhar.</p>
            <button className="secondary-btn" disabled>Em breve</button>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
