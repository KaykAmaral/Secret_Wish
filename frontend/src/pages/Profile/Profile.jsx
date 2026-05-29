import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { User, Mail, Shield, Camera, Check, ArrowLeft, Save, Trash2, LogOut } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import userService from '../../services/userService';
import './Profile.css';

/**
 * Página de Perfil do Usuário.
 * 
 * Permite que o usuário visualize seus dados, altere seu nome de exibição
 * e escolha um avatar da galeria pré-definida. Centraliza também ações de conta.
 */
const Profile = () => {
  const { user, checkAuth, logout } = useAuth();
  const navigate = useNavigate();
  
  // Estados locais para edição
  const [nome, setNome] = useState(user?.nome || '');
  const [email] = useState(user?.email || ''); // E-mail é apenas leitura para este fluxo
  const [selectedAvatar, setSelectedAvatar] = useState(user?.imagemUrl || '');
  
  // Estados de feedback
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');

  // Lista de avatares disponíveis na pasta public/avatars/
  const availableAvatars = Array.from({ length: 10 }, (_, i) => `/avatars/${i + 1}.png`);

  /**
   * Trata o salvamento das alterações do perfil.
   */
  const handleSave = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess(false);

    try {
      await userService.updateProfile({
        nome: nome.trim(),
        imagemUrl: selectedAvatar
      });
      // Revalida a sessão no AuthContext para atualizar o nome no Header e outros componentes.
      await checkAuth(); 
      setSuccess(true);
      // Remove a mensagem de sucesso após 3 segundos
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao atualizar perfil.');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Gera iniciais do nome para o caso de o usuário não possuir avatar selecionado.
   */
  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
  };

  return (
    <div className="profile-page">
      {/* Background Decorativo */}
      <div className="wishlist-background-decor" aria-hidden="true">
        <span className="gift gift-one">🎁</span>
        <span className="dashed-square square-one"></span>
      </div>

      <main className="profile-main">
        <nav className="profile-breadcrumb">
          <button onClick={() => navigate('/dashboard')}>
            <ArrowLeft size={18} /> Voltar para o Dashboard
          </button>
        </nav>

        <header className="profile-header">
          <div>
            <span className="profile-kicker">Sua conta</span>
            <h1>Meu Perfil</h1>
            <p>Personalize como você aparece para seus amigos secretos.</p>
          </div>
        </header>

        <div className="profile-layout">
          {/* Coluna da Esquerda: Formulário de Dados */}
          <section className="profile-card glass main-form-card">
            <form onSubmit={handleSave} className="profile-form">
              <div className="section-title-group">
                <User size={20} className="text-purple" />
                <h2>Informações Pessoais</h2>
              </div>

              <div className="form-grid">
                <div className="input-group">
                  <label>Nome Completo</label>
                  <div className="input-wrapper">
                    <User className="input-icon" size={18} />
                    <input 
                      type="text" 
                      value={nome} 
                      onChange={(e) => setNome(e.target.value)}
                      placeholder="Seu nome"
                      required
                    />
                  </div>
                </div>

                <div className="input-group disabled">
                  <label>E-mail (Não editável)</label>
                  <div className="input-wrapper">
                    <Mail className="input-icon" size={18} />
                    <input type="email" value={email} disabled />
                  </div>
                </div>
              </div>

              <div className="section-title-group avatar-section-title">
                <Camera size={20} className="text-purple" />
                <h2>Escolha seu Avatar</h2>
              </div>
              
              <div className="avatar-selection-grid">
                {availableAvatars.map((avatar) => (
                  <motion.div
                    key={avatar}
                    className={`avatar-option ${selectedAvatar === avatar ? 'selected' : ''}`}
                    onClick={() => setSelectedAvatar(avatar)}
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <img src={avatar} alt="Opção de Avatar" />
                    {selectedAvatar === avatar && (
                      <div className="check-badge">
                        <Check size={12} />
                      </div>
                    )}
                  </motion.div>
                ))}
              </div>

              <div className="form-footer">
                <AnimatePresence>
                  {success && (
                    <motion.p 
                      className="status-msg success"
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0 }}
                    >
                      Perfil atualizado com sucesso!
                    </motion.p>
                  )}
                  {error && (
                    <motion.p 
                      className="status-msg error"
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0 }}
                    >
                      {error}
                    </motion.p>
                  )}
                </AnimatePresence>
                
                <button type="submit" className="btn-primary save-btn" disabled={loading}>
                  <Save size={18} />
                  {loading ? 'Salvando...' : 'Salvar Alterações'}
                </button>
              </div>
            </form>
          </section>

          {/* Coluna da Direita: Preview e Segurança */}
          <aside className="profile-sidebar">
            {/* Cartão de Preview Visual */}
            <section className="profile-card glass preview-card">
              <div className="preview-avatar-container">
                {selectedAvatar ? (
                  <img src={selectedAvatar} alt="Preview" className="preview-avatar" />
                ) : (
                  <div className="preview-avatar-initials">{getInitials(nome)}</div>
                )}
                <div className="avatar-glow"></div>
              </div>
              <div className="preview-info">
                <h3>{nome || 'Seu Nome'}</h3>
                <p>{email}</p>
              </div>
            </section>

            {/* Ações de Segurança */}
            <section className="profile-card glass account-actions-card">
              <div className="section-title-group">
                <Shield size={20} className="text-purple" />
                <h2>Segurança e Conta</h2>
              </div>
              <div className="account-buttons">
                <button className="btn-secondary logout-btn" onClick={logout}>
                  <LogOut size={18} /> Sair da Conta
                </button>
                <button className="btn-danger-text delete-account-btn">
                  <Trash2 size={18} /> Excluir Conta permanentemente
                </button>
              </div>
            </section>
          </aside>
        </div>
      </main>
    </div>
  );
};

export default Profile;
