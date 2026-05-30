import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import groupService from '../../services/groupService';
import notificationService from '../../services/notificationService';
import './Dashboard.css';

/**
 * Página de Dashboard.
 * 
 * É a página principal após o login, oferecendo uma visão geral dos grupos do usuário,
 * notificações recentes e acesso rápido às funcionalidades essenciais como criar ou entrar em grupos.
 */
const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  
  // Estados de dados
  const [groups, setGroups] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Estados de controle de UI (Modais)
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showJoinModal, setShowJoinModal] = useState(false);
  
  // Estados de formulário para novos grupos
  const [newGroupName, setNewGroupName] = useState('');
  const [newGroupDesc, setNewGroupDesc] = useState('');
  const [newGroupDate, setNewGroupDate] = useState('');
  
  // Estados para entrada em grupo via código
  const [joinCode, setJoinCode] = useState('');
  
  // Estados de feedback e carregamento de ações
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [alertType, setAlertType] = useState('error');

  // Verifica se o usuário já é dono de um grupo (Regra de negócio: 1 grupo por dono)
  const hasCreatedGroup = groups.some(group => group.dono?.id === user?.id);

  /**
   * Formata um objeto Date para string yyyy-MM-dd, compatível com inputs HTML5.
   */
  const formatInputDate = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const todayDate = formatInputDate(new Date());

  /**
   * Calcula a data máxima permitida para eventos (hoje + 24 meses).
   */
  const maxEventDateValue = (() => {
    const maxDate = new Date();
    maxDate.setMonth(maxDate.getMonth() + 24);
    return formatInputDate(maxDate);
  })();

  /**
   * Formata e limpa a entrada do código do grupo enquanto o usuário digita.
   * Garante o formato XXXX-XXXX.
   */
  const formatGroupCode = (value) => {
    const cleaned = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase().slice(0, 8);
    if (cleaned.length <= 4) return cleaned;
    return `${cleaned.slice(0, 4)}-${cleaned.slice(4)}`;
  };

  /**
   * Calcula a diferença em dias entre hoje e a data do evento.
   */
  const getDaysUntilEvent = (dateValue) => {
    if (!dateValue) return null;
    const [year, month, day] = dateValue.split('T')[0].split('-').map(Number);
    const eventDate = new Date(year, month - 1, day);
    const today = new Date();
    const todayOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const millisecondsPerDay = 1000 * 60 * 60 * 24;
    return Math.ceil((eventDate - todayOnly) / millisecondsPerDay);
  };

  /**
   * Gera uma string descritiva para a contagem regressiva do evento.
   */
  const formatDaysUntilEvent = (dateValue) => {
    const days = getDaysUntilEvent(dateValue);
    if (days === null) return 'Data não definida';
    if (days < 0) return 'Data já passou';
    if (days === 0) return 'É hoje! 🎊';
    if (days === 1) return 'Falta 1 dia';
    return `Faltam ${days} dias`;
  };

  /**
   * Busca os dados iniciais do dashboard (grupos e notificações).
   */
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
      console.error('[Dashboard] Erro ao carregar dados:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  /**
   * Trata o envio do formulário de criação de grupo.
   */
  const handleCreateGroup = async (e) => {
    e.preventDefault();
    
    // Validações prévias no cliente
    if (hasCreatedGroup) {
      setAlertType('warning');
      setError('Você já possui um grupo criado.');
      return;
    }

    if (!newGroupName.trim() || !newGroupDesc.trim() || !newGroupDate) {
      setAlertType('warning');
      setError('Preencha nome, descrição e data do evento.');
      return;
    }

    if (newGroupDate && (newGroupDate < todayDate || newGroupDate > maxEventDateValue)) {
      setAlertType('warning');
      setError('A data do evento deve ser entre hoje e 24 meses.');
      return;
    }

    setActionLoading(true);
    setAlertType('error');
    setError('');
    
    try {
      const dateOnly = `${newGroupDate}T00:00:00`;
      const createdGroup = await groupService.createGroup({ 
        nome: newGroupName.trim(), 
        descricao: newGroupDesc.trim(),
        dataEvento: dateOnly 
      });
      setGroups(prev => [createdGroup, ...prev]);
      setShowCreateModal(false);
      // Limpa formulário após sucesso
      setNewGroupName('');
      setNewGroupDesc('');
      setNewGroupDate('');
    } catch (err) {
      setAlertType('error');
      setError(err.response?.data?.message || 'Erro ao criar grupo.');
    } finally {
      setActionLoading(false);
    }
  };

  /**
   * Trata o envio do formulário para entrar em um grupo via código.
   */
  const handleJoinGroup = async (e) => {
    e.preventDefault();
    if (!joinCode.trim()) {
      setAlertType('warning');
      setError('Informe o código do grupo.');
      return;
    }

    setActionLoading(true);
    setAlertType('error');
    setError('');
    try {
      await groupService.joinGroup(joinCode);
      setShowJoinModal(false);
      setJoinCode('');
      fetchData(); // Recarrega a lista de grupos
    } catch (err) {
      setAlertType('error');
      setError(err.response?.data?.message || 'Código inválido ou grupo já sorteado.');
    } finally {
      setActionLoading(false);
    }
  };

  /**
   * Marca uma notificação individual como lida.
   */
  const markAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications(notifications.map(n => n.id === id ? { ...n, lida: true } : n));
    } catch (err) {
      console.error('[Dashboard] Erro ao marcar notificação:', err);
    }
  };

  /**
   * Exclui uma notificação do histórico do usuário.
   */
  const deleteNotification = async (id) => {
    try {
      await notificationService.deleteNotification(id);
      setNotifications(notifications.filter(n => n.id !== id));
    } catch (err) {
      console.error('[Dashboard] Erro ao excluir notificação:', err);
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
      {/* Elementos decorativos animados por CSS */}
      <div className="dashboard-background-decor" aria-hidden="true">
        <span className="decor-gift gift-one">🎁</span>
        <span className="decor-gift gift-two">🎁</span>
        <span className="decor-person person-one">👤</span>
        <span className="decor-person person-two">👥</span>
      </div>

      <main className="dashboard-main">
        {/* Boas-vindas e Ações Rápidas */}
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
              title={hasCreatedGroup ? 'Você já possui um grupo criado' : 'Criar grupo'}
            >
              <span>+</span> Criar Grupo
            </button>
            <button className="btn-secondary" onClick={() => setShowJoinModal(true)}>
              <span>#</span> Entrar via Código
            </button>
            <button className="btn-wishlist" onClick={() => navigate('/wishlist')}>
              <span>♥</span> Lista de Desejos
            </button>
          </div>
        </section>

        <div className="dashboard-grid">
          {/* Listagem de Grupos Ativos */}
          <div className="grid-main" id="meus-grupos">
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
                      {/* A listagem do backend envia totalMembros para evitar carregar todos os usuarios. */}
                      <p><strong>Participantes:</strong> {group.totalMembros ?? group.membros?.length ?? 0}</p>
                      <p><strong>Evento:</strong> {group.dataEvento ? new Date(group.dataEvento).toLocaleDateString() : 'Não definida'}</p>
                      <p><strong>Contagem:</strong> {formatDaysUntilEvent(group.dataEvento)}</p>
                    </div>
                    <button className="btn-view">
                      {group.dono?.id === user?.id ? 'Gerenciar Grupo' : 'Visualizar Grupo'}
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Sidebar de Notificações */}
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

      {/* --- Modais --- */}

      {/* Modal de Criação de Grupo */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-card glass">
            <div className="modal-header">
              <h2>Criar Novo Grupo</h2>
              <button className="close-btn" onClick={() => setShowCreateModal(false)}>&times;</button>
            </div>
            <form onSubmit={handleCreateGroup} className="modal-form" noValidate>
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
                <label>Descrição</label>
                <textarea 
                  placeholder="Ex: Amigo secreto de natal da família..." 
                  value={newGroupDesc}
                  onChange={(e) => setNewGroupDesc(e.target.value)}
                  required
                />
              </div>
              <div className="input-group">
                <label>Data do Evento</label>
                <input
                  type="date"
                  value={newGroupDate}
                  min={todayDate}
                  max={maxEventDateValue}
                  onChange={(e) => setNewGroupDate(e.target.value)}
                  required
                />
              </div>
              {error && <p className={`form-alert ${alertType}`}>{error}</p>}
              <button type="submit" className="btn-primary" disabled={actionLoading || hasCreatedGroup}>
                {actionLoading ? 'Criando...' : 'Criar Grupo'}
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Modal de Entrada em Grupo via Código */}
      {showJoinModal && (
        <div className="modal-overlay">
          <div className="modal-card glass">
            <div className="modal-header">
              <h2>Entrar em um Grupo</h2>
              <button className="close-btn" onClick={() => setShowJoinModal(false)}>&times;</button>
            </div>
            <form onSubmit={handleJoinGroup} className="modal-form" noValidate>
              <div className="input-group">
                <label>Código do Grupo</label>
                <input 
                  type="text" 
                  placeholder="XXXX-XXXX" 
                  value={joinCode}
                  onChange={(e) => setJoinCode(formatGroupCode(e.target.value))}
                  maxLength="9"
                  required 
                />
                <p className="helper-text">Peça o código para quem criou o grupo.</p>
              </div>
              {error && <p className={`form-alert ${alertType}`}>{error}</p>}
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
