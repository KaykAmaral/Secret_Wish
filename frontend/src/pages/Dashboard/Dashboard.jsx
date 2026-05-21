import { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import groupService from '../../services/groupService';
import notificationService from '../../services/notificationService';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [groups, setGroups] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showJoinModal, setShowJoinModal] = useState(false);
  
  // States para formulários
  const [newGroupName, setNewGroupName] = useState('');
  const [newGroupDate, setNewGroupDate] = useState('');
  const [joinCode, setJoinCode] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
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
  };

  const handleCreateGroup = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    setError('');
    try {
      await groupService.createGroup({ nome: newGroupName, dataEvento: newGroupDate });
      setShowCreateModal(false);
      setNewGroupName('');
      setNewGroupDate('');
      fetchData();
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
      <header className="dashboard-header">
        <div className="header-content">
          <div className="logo">🎁 <span className="logo-text">Secret Wish</span></div>
          <div className="user-nav">
            <div className="notification-badge-container">
              <span className="icon">🔔</span>
              {notifications.filter(n => !n.lida).length > 0 && (
                <span className="badge">{notifications.filter(n => !n.lida).length}</span>
              )}
            </div>
            <div className="user-info">
              <span className="user-name">{user?.nome}</span>
              <button className="logout-btn" onClick={logout}>Sair</button>
            </div>
          </div>
        </div>
      </header>

      <main className="dashboard-main">
        <section className="welcome-section">
          <div className="welcome-text">
            <h1>Olá, {user?.nome?.split(' ')[0]}! 👋</h1>
            <p>Pronto para mais um amigo secreto inesquecível?</p>
          </div>
          <div className="actions-bar">
            <button className="btn-primary" onClick={() => setShowCreateModal(true)}>
              <span>+</span> Criar Grupo
            </button>
            <button className="btn-secondary" onClick={() => setShowJoinModal(true)}>
              <span>#</span> Entrar via Código
            </button>
          </div>
        </section>

        <div className="dashboard-grid">
          {/* Listagem de Grupos */}
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
                  <div key={group.id} className="group-card glass">
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
                    <button className="btn-view" onClick={() => window.location.href = `/groups/${group.id}`}>
                      Gerenciar Grupo
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Notificações */}
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

      {/* Modal Criar Grupo */}
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
                <label>Data do Evento (Opcional)</label>
                <input 
                  type="datetime-local" 
                  value={newGroupDate}
                  onChange={(e) => setNewGroupDate(e.target.value)}
                />
              </div>
              {error && <p className="error-msg">{error}</p>}
              <button type="submit" className="btn-primary" disabled={actionLoading}>
                {actionLoading ? 'Criando...' : 'Criar Grupo'}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Modal Entrar no Grupo */}
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
    </div>
  );
};

export default Dashboard;
