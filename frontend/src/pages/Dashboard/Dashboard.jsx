import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import groupService from '../../services/groupService';
import notificationService from '../../services/notificationService';
import WishlistModal from '../../components/WishlistModal/WishlistModal';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [groups, setGroups] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Modals visibility
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [showWishlistModal, setShowWishlistModal] = useState(false);
  
  // States para formulários
  const [newGroupName, setNewGroupName] = useState('');
  const [newGroupDesc, setNewGroupDesc] = useState('');
  const [newGroupDate, setNewGroupDate] = useState('');
  const [joinCode, setJoinCode] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const hasCreatedGroup = groups.some(group => group.dono?.id === user?.id);

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      const [groupsData, notificationsData] = await Promise.all([
        groupService.getMyGroups(),
        notificationService.getNotifications()
      ]);
      setGroups(groupsData);
      setNotifications(notificationsData);
    } catch (err) {
      console.error('Erro ao carregar dados do dashboard:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchData();
    }, 0);
    return () => clearTimeout(timer);
  }, [fetchData]);

  const handleCreateGroup = async (e) => {
    e.preventDefault();
    if (hasCreatedGroup) {
      setError('Voce ja possui um grupo criado.');
      return;
    }

    setActionLoading(true);
    setError('');
    try {
      const dateOnly = newGroupDate ? `${newGroupDate}T00:00:00` : null;
      const createdGroup = await groupService.createGroup({ 
        nome: newGroupName, 
        descricao: newGroupDesc,
        dataEvento: dateOnly 
      });
      setGroups(previousGroups => [createdGroup, ...previousGroups]);
      setShowCreateModal(false);
      setNewGroupName('');
      setNewGroupDesc('');
      setNewGroupDate('');
      await fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao criar grupo.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleJoinGroup = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    setError('');
    try {
      await groupService.joinGroup(joinCode);
      setShowJoinModal(false);
      setJoinCode('');
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Código inválido ou grupo já sorteado.');
    } finally {
      setActionLoading(false);
    }
  };

  const markAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications(notifications.map(n => n.id === id ? { ...n, lida: true } : n));
    } catch (err) {
      console.error('Erro ao marcar como lida:', err);
    }
  };

  const deleteNotification = async (id) => {
    try {
      await notificationService.deleteNotification(id);
      setNotifications(notifications.filter(n => n.id !== id));
    } catch (err) {
      console.error('Erro ao excluir notificação:', err);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Carregando seu painel...</p>
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      <main className="dashboard-main">
        <section className="welcome-section">
          <div className="welcome-text">
            <h1>Olá, {user?.nome?.split(' ')[0]}! 👋</h1>
            <p>Pronto para mais um amigo secreto inesquecível?</p>
          </div>
          <div className="actions-bar">
            <button
              className="btn-primary"
              onClick={() => setShowCreateModal(true)}
              disabled={hasCreatedGroup}
              title={hasCreatedGroup ? 'Voce ja possui um grupo criado' : 'Criar grupo'}
            >
              <span>+</span> Criar Grupo
            </button>
            <button className="btn-secondary" onClick={() => setShowJoinModal(true)}>
              <span>#</span> Entrar via Código
            </button>
            <button className="btn-wishlist" onClick={() => setShowWishlistModal(true)}>
              <span>♥</span> Lista de Desejos
            </button>
          </div>
        </section>

        <div className="dashboard-grid">
          <div className="grid-main">
            <h2 className="section-title">Seus Grupos</h2>
            {groups.length === 0 ? (
              <div className="empty-state glass">
                <div className="empty-icon">🎄</div>
                <h3>Nenhum grupo ainda</h3>
                <p>Crie um grupo ou use um código para entrar em um existente.</p>
              </div>
            ) : (
              <div className="groups-list">
                {groups.map(group => (
                  <div key={group.id} className="group-card glass" onClick={() => navigate(`/groups/${group.id}`)}>
                    <div className="group-header">
                      <h3>{group.nome}</h3>
                      <span className={`status-tag ${group.dataSorteio ? 'drawn' : 'pending'}`}>
                        {group.dataSorteio ? 'Sorteado' : 'Aguardando'}
                      </span>
                    </div>
                    <div className="group-info">
                      <p><strong>Código:</strong> <code>{group.codigoUnico}</code></p>
                      <p><strong>Participantes:</strong> {group.membros?.length || 0}</p>
                      <p><strong>Evento:</strong> {group.dataEvento ? new Date(group.dataEvento).toLocaleDateString() : 'Não definida'}</p>
                    </div>
                    <button className="btn-view">Gerenciar Grupo</button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <aside className="grid-side">
            <div className="side-header">
              <h2 className="section-title">Notificações</h2>
              {notifications.length > 0 && (
                <button className="btn-text" onClick={() => notificationService.deleteAllNotifications().then(fetchData)}>
                  Limpar tudo
                </button>
              )}
            </div>
            <div className="notifications-container glass">
              {notifications.length === 0 ? (
                <p className="empty-notifications">Nenhuma notificação por enquanto.</p>
              ) : (
                notifications.map(notif => (
                  <div key={notif.id} className={`notif-item ${!notif.lida ? 'unread' : ''}`}>
                    <div className="notif-content">
                      <h4>{notif.titulo}</h4>
                      <p>{notif.mensagem}</p>
                      <span className="notif-time">{new Date(notif.dataCriacao).toLocaleDateString()}</span>
                    </div>
                    <div className="notif-actions">
                      {!notif.lida && <button title="Marcar como lida" onClick={() => markAsRead(notif.id)}>✅</button>}
                      <button title="Excluir" onClick={() => deleteNotification(notif.id)}>🗑️</button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </aside>
        </div>
      </main>

      {/* Modais */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-card glass">
            <div className="modal-header">
              <h2>Criar Novo Grupo</h2>
              <button className="close-btn" onClick={() => setShowCreateModal(false)}>&times;</button>
            </div>
            <form onSubmit={handleCreateGroup} className="modal-form">
              <div className="input-group">
                <label>Nome do Grupo</label>
                <input 
                  type="text" 
                  placeholder="Ex: Família Silva 2026" 
                  value={newGroupName}
                  onChange={(e) => setNewGroupName(e.target.value)}
                  required 
                />
              </div>
              <div className="input-group">
                <label>Descrição (Opcional)</label>
                <textarea 
                  placeholder="Ex: Amigo secreto de natal da família..." 
                  value={newGroupDesc}
                  onChange={(e) => setNewGroupDesc(e.target.value)}
                />
              </div>
              <div className="input-group">
                <label>Data do Evento (Opcional)</label>
                <input type="date" value={newGroupDate} onChange={(e) => setNewGroupDate(e.target.value)} />
              </div>
              {error && <p className="error-msg">{error}</p>}
              <button type="submit" className="btn-primary" disabled={actionLoading || hasCreatedGroup}>
                {actionLoading ? 'Criando...' : 'Criar Grupo'}
              </button>
            </form>
          </div>
        </div>
      )}

      {showJoinModal && (
        <div className="modal-overlay">
          <div className="modal-card glass">
            <div className="modal-header">
              <h2>Entrar em um Grupo</h2>
              <button className="close-btn" onClick={() => setShowJoinModal(false)}>&times;</button>
            </div>
            <form onSubmit={handleJoinGroup} className="modal-form">
              <div className="input-group">
                <label>Código do Grupo</label>
                <input 
                  type="text" 
                  placeholder="XXXX-XXXX" 
                  value={joinCode}
                  onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
                  maxLength="9"
                  required 
                />
                <p className="helper-text">Peça o código para quem criou o grupo.</p>
              </div>
              {error && <p className="error-msg">{error}</p>}
              <button type="submit" className="btn-primary" disabled={actionLoading}>
                {actionLoading ? 'Entrando...' : 'Entrar no Grupo'}
              </button>
            </form>
          </div>
        </div>
      )}

      <WishlistModal isOpen={showWishlistModal} onClose={() => setShowWishlistModal(false)} />
    </div>
  );
};

export default Dashboard;
