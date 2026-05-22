import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const MainLayout = () => {
  const { isAuthenticated, logout, user } = useAuth();

  if (!isAuthenticated) {
    return <Outlet />;
  }

  return (
    <div className="app-layout">
      <header className="dashboard-header">
        <div className="header-content">
          <Link to="/dashboard" className="logo">🎁 <span className="logo-text">Secret Wish</span></Link>
          <nav className="main-nav">
            <Link to="/dashboard" className="nav-link">Início</Link>
          </nav>
          <div className="user-nav">
            <div className="user-info">
              <span className="user-name">{user?.nome}</span>
              <button className="logout-btn" onClick={logout}>Sair</button>
            </div>
          </div>
        </div>
      </header>
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  );
};

export default MainLayout;
