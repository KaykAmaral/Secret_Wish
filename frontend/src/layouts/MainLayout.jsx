import { useState, useRef, useEffect } from 'react';
import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import ProfileModal from '../components/ProfileModal/ProfileModal';
import './MainLayout.css';

const MainLayout = () => {
  const { isAuthenticated, logout, user, checkAuth } = useAuth();
  const [showDropdown, setShowDropdown] = useState(false);
  const [showProfileModal, setShowProfileModal] = useState(false);
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

  // Telas publicas usam apenas o Outlet, sem chrome autenticado.
  if (!isAuthenticated) {
    return <Outlet />;
  }

  const toggleDropdown = () => setShowDropdown(!showDropdown);

  // Abre o modal e fecha o dropdown para manter apenas uma superficie ativa.
  const handleOpenProfile = () => {
    setShowProfileModal(true);
    setShowDropdown(false);
  };

  // Usa ate duas iniciais para manter avatar legivel quando nao ha imagem.
  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
  };

  return (
    <div className="app-layout">
      <header className="dashboard-header">
        <div className="header-content">
          <Link to="/dashboard" className="logo">
            <span className="logo-icon">🎁</span>
            <span className="logo-text">Secret Wish</span>
          </Link>

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
                <button className="dropdown-item" onClick={handleOpenProfile}>
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

      <main className="app-content">
        <Outlet />
      </main>

      <ProfileModal 
        isOpen={showProfileModal} 
        onClose={() => setShowProfileModal(false)}
        onUpdate={() => checkAuth()} 
      />
    </div>
  );
};

export default MainLayout;
