import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import './Auth.css';

/**
 * Página de Cadastro de Usuário (Register).
 * 
 * Permite a criação de uma nova conta utilizando e-mail, nome e senha.
 * Também oferece integração com o cadastro rápido via Google.
 */
const Register = () => {
  const { login: loginGoogle, registerWithEmail } = useAuth();
  const navigate = useNavigate();
  
  // Estados locais para o formulário de cadastro
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [alertType, setAlertType] = useState('error');

  /**
   * Trata o envio do formulário de registro por e-mail.
   */
  const handleRegister = async (e) => {
    e.preventDefault();
    
    // Validação de confirmação de senha no lado do cliente
    if (password !== confirmPassword) {
      setAlertType('warning');
      setError('As senhas não coincidem.');
      return;
    }

    setLoading(true);
    setAlertType('error');
    setError('');
    
    try {
      // O registerWithEmail realiza o cadastro e já deixa o usuário logado no backend.
      const success = await registerWithEmail(nome, email, password);
      if (success) {
        navigate('/dashboard');
      } else {
        setError('Falha ao processar o cadastro.');
      }
    } catch (err) {
      console.error('[Register] Erro ao cadastrar usuário:', err);
      const backendMessage = err.response?.data?.message;
      setError(backendMessage || 'Erro de conexão com o servidor.');
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
              <div className="feature-icon">✨</div>
              <div className="feature-text">
                <h3>Totalmente Grátis</h3>
                <p>Organize quantos grupos quiser sem custos adicionais.</p>
              </div>
            </div>
            <div className="feature-item">
              <div className="feature-icon">🕵️</div>
              <div className="feature-text">
                <h3>Privacidade Garantida</h3>
                <p>Seus dados e sorteios são protegidos com criptografia.</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Lado Direito - Formulário de Cadastro */}
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

              {error && <div className={`auth-alert ${alertType}`}>{error}</div>}

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
