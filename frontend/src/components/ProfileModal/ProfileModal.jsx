import { useState } from 'react';
import userService from '../../services/userService';
import { useAuth } from '../../hooks/useAuth';
import './ProfileModal.css';

const ProfileModal = ({ isOpen, onClose, onUpdate }) => {
  const { user, logout } = useAuth();
  const [nome, setNome] = useState(user?.nome || '');
  const [imagemUrl, setImagemUrl] = useState(user?.imagemUrl || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  // Atualiza o perfil e delega ao layout a revalidacao do usuario exibido no header.
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const updatedUser = await userService.updateProfile({ nome, imagemUrl });
      onUpdate(updatedUser);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao atualizar perfil.');
    } finally {
      setLoading(false);
    }
  };

  // Depois da exclusao, logout limpa cookie e estado local antes de voltar ao login.
  const handleDeleteAccount = async () => {
    setLoading(true);
    try {
      await userService.deleteAccount();
      logout(); // Faz logout e redireciona para login
    } catch {
      setError('Erro ao excluir conta.');
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-card glass profile-modal animate-in">
        <div className="modal-header">
          <h2>Configurações de Perfil</h2>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </div>

        <div className="profile-modal-body">
          {!showDeleteConfirm ? (
            <form onSubmit={handleSubmit} className="modal-form">
              <div className="avatar-section">
                <div className="avatar-preview">
                  {imagemUrl ? (
                    <img src={imagemUrl} alt="Preview" />
                  ) : (
                    <div className="avatar-placeholder">{nome.charAt(0) || 'U'}</div>
                  )}
                  <div className="glow"></div>
                </div>
                <div className="input-group">
                  <label>URL da Foto de Perfil</label>
                  <input 
                    type="url" 
                    placeholder="https://..." 
                    value={imagemUrl}
                    onChange={(e) => setImagemUrl(e.target.value)}
                  />
                </div>
              </div>

              <div className="input-group">
                <label>Seu Nome</label>
                <input 
                  type="text" 
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required 
                />
              </div>

              <div className="input-group">
                <label>Email (Não pode ser alterado)</label>
                <input type="email" value={user?.email} disabled className="disabled-input" />
              </div>

              {error && <p className="form-alert error">{error}</p>}

              <div className="form-actions">
                <button type="button" className="btn-danger-outline" onClick={() => setShowDeleteConfirm(true)}>Excluir Conta</button>
                <button type="submit" className="btn-primary" disabled={loading}>
                  {loading ? 'Salvando...' : 'Salvar Alterações'}
                </button>
              </div>
            </form>
          ) : (
            <div className="delete-confirm-area animate-in">
              <div className="warning-icon">⚠️</div>
              <h3>Excluir Conta Permanentemente?</h3>
              <p>Esta ação é <strong>irreversível</strong>. Todos os seus grupos, listas e mensagens serão apagados para sempre.</p>
              
              <div className="confirm-actions">
                <button className="btn-secondary" onClick={() => setShowDeleteConfirm(false)}>Cancelar</button>
                <button className="btn-danger" onClick={handleDeleteAccount} disabled={loading}>
                  {loading ? 'Excluindo...' : 'Sim, Excluir Minha Conta'}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProfileModal;
