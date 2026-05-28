import { useState, useRef, useEffect } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import './MainLayout.css';

const MainLayout = () => {
  const { isAuthenticated, logout, user, loading } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);

  // Fecha o menu de perfil ao clicar fora sem registrar listener enquanto ele esta fechado.
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    };

    if (showDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showDropdown]);

  const toggleDropdown = () => setShowDropdown(!showDropdown);

  const handleGoToProfile = () => {
    setShowDropdown(false);
    navigate('/profile');
  };

  // Se estiver carregando o status inicial, mostra um spinner global para evitar saltos de layout.
  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Carregando Secret Wish...</p>
      </div>
    );
  }

  // Usa ate duas iniciais para manter avatar legivel quando nao ha imagem.
  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
  };

  return (
    <div className="app-layout">
      {/* O header so aparece se o usuario estiver autenticado */}
      {isAuthenticated && (
        <header className="dashboard-header">
          <div className="header-content">
            <Link to="/dashboard" className="logo">
              <span className="logo-icon">🎁</span>
              <span className="logo-text">Secret Wish</span>
            </Link>

            <nav className="main-nav" aria-label="Navegacao principal">
              <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                Pagina principal
              </NavLink>
              <NavLink to="/my-groups" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                Meus grupos
              </NavLink>
              <NavLink to="/wishlist" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                Lista de desejos
              </NavLink>
              <NavLink to="/profile" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                Meu perfil
              </NavLink>
            </nav>

            <div className="user-nav-container" ref={dropdownRef}>
              <div className="user-profile-trigger" onClick={toggleDropdown}>
                <div className="user-text">
                  <span className="user-name">{user?.nome}</span>
                  <span className="user-status">Online</span>
                </div>
                <div className="user-avatar-container">
                  {user?.imagemUrl ? (
                    <img src={user.imagemUrl} alt="Avatar" className="user-avatar" />
                  ) : (
                    <div className="user-avatar-initials">{getInitials(user?.nome)}</div>
                  )}
                  <div className="avatar-glow"></div>
                </div>
              </div>

              {showDropdown && (
                <div className="profile-dropdown glass animate-in">
                  <div className="dropdown-header">
                    <p className="dropdown-user-email">{user?.email}</p>
                  </div>
                  <div className="dropdown-divider"></div>
                  <button className="dropdown-item" onClick={handleGoToProfile}>
                    <span className="item-icon">👤</span> Perfil & Conta
                  </button>
                  <button className="dropdown-item" onClick={() => setShowDropdown(false)}>
                    <span className="item-icon">⚙️</span> Configurações
                  </button>
                  <button className="dropdown-item" onClick={() => setShowDropdown(false)}>
                    <span className="item-icon">🔒</span> Privacidade
                  </button>
                  <div className="dropdown-divider"></div>
                  <button className="dropdown-item logout" onClick={logout}>
                    <span className="item-icon">🚪</span> Sair
                  </button>
                </div>
              )}
            </div>
          </div>
        </header>
      )}

      <main className={isAuthenticated ? "app-content" : "auth-content-wrapper"}>
        <Outlet />
      </main>
    </div>
  );
};

export default MainLayout;
