import { useState, useRef, useEffect } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import './MainLayout.css';

/**
 * Layout Principal da Aplicação (MainLayout).
 * 
 * Este componente define a estrutura global da interface, incluindo o Header (cabeçalho)
 * fixo e a área de conteúdo dinâmico (Outlet). Ele gerencia a exibição de elementos
 * baseada no estado de autenticação e provê o menu de perfil do usuário.
 */
const MainLayout = () => {
  const { isAuthenticated, logout, user, loading } = useAuth();
  const navigate = useNavigate();
  
  // Estado para controlar a visibilidade do menu suspenso (dropdown) de perfil
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);

  /**
   * Efeito para fechar o dropdown ao clicar em qualquer lugar fora dele.
   * Melhora a experiência de uso (UX) e evita menus órfãos na tela.
   */
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

  /**
   * Redireciona para a página de perfil e fecha o menu.
   */
  const handleGoToProfile = () => {
    setShowDropdown(false);
    navigate('/profile');
  };

  /**
   * Exibe uma tela de carregamento global enquanto o status inicial de autenticação
   * está sendo verificado pelo AuthProvider.
   */
  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Carregando Secret Wish...</p>
      </div>
    );
  }

  /**
   * Gera as iniciais do usuário para exibição no avatar caso não haja imagemUrl.
   */
  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
  };

  return (
    <div className="app-layout">
      {/* O Header só é renderizado se o usuário estiver logado */}
      {isAuthenticated && (
        <header className="dashboard-header">
          <div className="header-content">
            {/* Logo e Link para Home */}
            <Link to="/dashboard" className="logo">
              <span className="logo-icon">🎁</span>
              <span className="logo-text">Secret Wish</span>
            </Link>

            {/* Navegação Principal */}
            <nav className="main-nav" aria-label="Navegação principal">
              <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                Dashboard
              </NavLink>
              <NavLink to="/my-groups" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                Meus Grupos
              </NavLink>
              <NavLink to="/wishlist" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                Lista de Desejos
              </NavLink>
              <NavLink to="/profile" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                Perfil
              </NavLink>
            </nav>

            {/* Menu do Usuário (Avatar e Dropdown) */}
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

              {/* Menu Suspenso Animado */}
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

      {/* Área de Conteúdo Principal */}
      <main className={isAuthenticated ? "app-content" : "auth-content-wrapper"}>
        {/* O Outlet renderiza o componente da rota ativa definida em AppRoutes.jsx */}
        <Outlet />
      </main>
    </div>
  );
};

export default MainLayout;
