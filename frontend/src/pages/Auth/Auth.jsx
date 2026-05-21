import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import './Auth.css';

const Auth = () => {
  const { login: loginGoogle, loginWithEmail } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleEmailLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const success = await loginWithEmail(email, password);
      if (success) {
        navigate('/dashboard');
      } else {
        setError('Falha ao autenticar.');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao entrar. Verifique suas credenciais.');
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
          
          <h2 className="hero-title">Onde a mágica do <span className="highlight">Amigo Secreto</span> acontece.</h2>
          <p className="hero-description">
            Organize sorteios, crie listas de desejos inteligentes e converse anonimamente. 
            Tudo em um só lugar, potencializado por IA.
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
            <h2 className="section-title">Entrar</h2>
            <p className="section-subtitle">Acesse sua conta com e-mail ou via Google.</p>
            
            <form onSubmit={handleEmailLogin} className="auth-form">
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
                  placeholder="••••••••" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required 
                />
              </div>

              {error && <div className="auth-error">{error}</div>}

              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? <span className="spinner-small"></span> : 'Entrar'}
              </button>
            </form>

            <div className="divider">
              <span>OU</span>
            </div>
            
            <button className="btn-google" onClick={loginGoogle} disabled={loading}>
              <img src="https://authjs.dev/img/providers/google.svg" alt="Google" width="20" height="20" />
              <span>Entrar com Google</span>
            </button>

            <p className="auth-switch">
              Não tem uma conta? <Link to="/register">Cadastre-se</Link>
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

export default Auth;
