import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import authService from '../../services/authService';
import './Auth.css';

const Register = () => {
  const { login: loginGoogle, checkAuth } = useAuth();
  const navigate = useNavigate();
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleRegister = async (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setError('As senhas não coincidem.');
      return;
    }

    setLoading(true);
    setError('');
    try {
      await authService.register(nome, email, password);
      await checkAuth();
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao cadastrar. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      {/* Lado Esquerdo - Institucional */}
      <div className="auth-info-side">
        <div className="info-content">
          <div className="brand">
            <span className="brand-icon">🎁</span>
            <h1 className="brand-name">Secret Wish</h1>
          </div>
          
          <h2 className="hero-title">Crie sua conta e <span className="highlight">espalhe alegria</span>.</h2>
          <p className="hero-description">
            Cadastre-se em segundos e comece a organizar seu amigo secreto com o poder da tecnologia.
          </p>

          <div className="feature-cards">
            <div className="feature-item">
              <div className="feature-icon">👥</div>
              <div className="feature-text">
                <h3>Grupos Inteligentes</h3>
                <p>Crie e gerencie grupos com códigos de acesso únicos.</p>
              </div>
            </div>
            <div className="feature-item">
              <div className="feature-icon">✨</div>
              <div className="feature-text">
                <h3>Wishlist Dinâmica</h3>
                <p>Adicione links e deixe que seus amigos saibam o que você quer.</p>
              </div>
            </div>
            <div className="feature-item">
              <div className="feature-icon">🕵️</div>
              <div className="feature-text">
                <h3>Chat Anônimo</h3>
                <p>Tire dúvidas com seu amigo secreto sem revelar sua identidade.</p>
              </div>
        
            </div>
          </div>
        </div>
      </div>

      {/* Lado Direito - Autenticação */}
      <div className="auth-form-side">
        <div className="auth-card glass">
          <section className="auth-section">
            <h2 className="section-title">Cadastrar</h2>
            <p className="section-subtitle">Crie sua conta rapidamente ou use o Google.</p>
            
            <form onSubmit={handleRegister} className="auth-form">
              <div className="input-group">
                <label htmlFor="nome">Nome Completo</label>
                <input 
                  type="text" 
                  id="nome" 
                  placeholder="Seu nome" 
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required 
                />
              </div>
              <div className="input-group">
                <label htmlFor="email">E-mail</label>
                <input 
                  type="email" 
                  id="email" 
                  placeholder="seu@email.com" 
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required 
                />
              </div>
              <div className="input-group">
                <label htmlFor="password">Senha</label>
                <input 
                  type="password" 
                  id="password" 
                  placeholder="No mínimo 6 caracteres" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required 
                />
              </div>
              <div className="input-group">
                <label htmlFor="confirmPassword">Confirmar Senha</label>
                <input 
                  type="password" 
                  id="confirmPassword" 
                  placeholder="••••••••" 
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required 
                />
              </div>

              {error && <div className="auth-error">{error}</div>}

              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? <span className="spinner-small"></span> : 'Cadastrar'}
              </button>
            </form>

            <div className="divider">
              <span>OU</span>
            </div>
            
            <button className="btn-google" onClick={loginGoogle} disabled={loading}>
              <img src="https://authjs.dev/img/providers/google.svg" alt="Google" width="20" height="20" />
              <span>Cadastrar com Google</span>
            </button>

            <p className="auth-switch">
              Já possui conta? <Link to="/login">Entrar</Link>
            </p>
          </section>

          <footer className="auth-footer">
            <p>© 2026 Secret Wish. A plataforma inteligente de Amigo Secreto.</p>
          </footer>
        </div>
      </div>
    </div>
  );
};

export default Register;
