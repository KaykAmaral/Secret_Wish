import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Users, Plus, Hash, Calendar, Clock, ArrowRight, Search, Filter, Hash as HashIcon, ArrowLeft } from 'lucide-react';
import groupService from '../../services/groupService';
import { useAuth } from '../../hooks/useAuth';
import './MyGroups.css';

const MyGroups = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filter, setFilter] = useState('all'); // 'all' | 'owner' | 'member' | 'drawn' | 'pending'

  // States para o modal de entrar em grupo
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [joinCode, setJoinCode] = useState('');
  const [newGroupName, setNewGroupName] = useState('');
  const [newGroupDesc, setNewGroupDesc] = useState('');
  const [newGroupDate, setNewGroupDate] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [alertType, setAlertType] = useState('error');

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      const data = await groupService.getMyGroups();
      setGroups(data);
    } catch (err) {
      console.error('Erro ao carregar grupos:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const formatGroupCode = (value) => {
    const cleaned = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase().slice(0, 8);
    if (cleaned.length <= 4) return cleaned;
    return `${cleaned.slice(0, 4)}-${cleaned.slice(4)}`;
  };

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
      fetchData();
    } catch (err) {
      setAlertType('error');
      setError(err.response?.data?.message || 'Código inválido ou grupo já sorteado.');
    } finally {
      setActionLoading(false);
    }
  };

  const hasCreatedGroup = groups.some(group => group.dono?.id === user?.id);
  const todayDate = new Date().toISOString().split('T')[0];
  const maxEventDateValue = (() => {
    const maxDate = new Date();
    maxDate.setMonth(maxDate.getMonth() + 24);
    return maxDate.toISOString().split('T')[0];
  })();

  const handleCreateGroup = async (e) => {
    e.preventDefault();
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

    setActionLoading(true);
    setAlertType('error');
    setError('');
    try {
      const dateOnly = `${newGroupDate}T00:00:00`;
      await groupService.createGroup({ 
        nome: newGroupName.trim(), 
        descricao: newGroupDesc.trim(),
        dataEvento: dateOnly 
      });
      setShowCreateModal(false);
      setNewGroupName('');
      setNewGroupDesc('');
      setNewGroupDate('');
      fetchData();
    } catch (err) {
      setAlertType('error');
      setError(err.response?.data?.message || 'Erro ao criar grupo.');
    } finally {
      setActionLoading(false);
    }
  };

  const filteredGroups = groups.filter(group => {
    const matchesSearch = group.nome.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          group.codigoUnico.toLowerCase().includes(searchTerm.toLowerCase());
    
    if (!matchesSearch) return false;

    if (filter === 'owner') return group.dono?.id === user?.id;
    if (filter === 'member') return group.dono?.id !== user?.id;
    if (filter === 'drawn') return !!group.dataSorteio;
    if (filter === 'pending') return !group.dataSorteio;
    
    return true;
  });

  const getDaysUntilEvent = (dateValue) => {
    if (!dateValue) return null;
    const [year, month, day] = dateValue.split('T')[0].split('-').map(Number);
    const eventDate = new Date(year, month - 1, day);
    const today = new Date();
    const todayOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    return Math.ceil((eventDate - todayOnly) / (1000 * 60 * 60 * 24));
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Carregando seus grupos...</p>
      </div>
    );
  }

  return (
    <div className="my-groups-page">
      {/* Background Decorativo da Lista de Desejos */}
      <div className="wishlist-background-decor" aria-hidden="true">
        <span className="gift gift-one">🎁</span>
        <span className="gift gift-two">🎁</span>
        <span className="gift gift-three">🎁</span>
        <span className="gift gift-four">🎁</span>
        <span className="dashed-square square-one"></span>
        <span className="dashed-square square-two"></span>
        <span className="dashed-square square-three"></span>
        <span className="dashed-square square-four"></span>
      </div>

      <main className="my-groups-main">
        <nav className="my-groups-breadcrumb">
          <button onClick={() => navigate('/dashboard')}>
            <ArrowLeft size={18} /> Voltar para o Dashboard
          </button>
        </nav>

        <header className="my-groups-header">
          <motion.div 
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
          >
            <span className="my-groups-kicker">Gerenciamento central</span>
            <h1>Meus Grupos</h1>
            <p>Acompanhe e administre todos os seus amigos secretos em um só lugar.</p>
          </motion.div>
          <motion.div 
            className="header-actions"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
          >
            <button className="btn-primary" onClick={() => setShowCreateModal(true)} disabled={hasCreatedGroup}>
              <Plus size={20} /> Novo Grupo
            </button>
            <button className="btn-secondary" onClick={() => setShowJoinModal(true)}>
              <HashIcon size={20} /> Entrar no Grupo
            </button>
          </motion.div>
        </header>

        {error && <p className={`form-alert ${alertType}`}>{error}</p>}

        <div className="my-groups-layout">
          <section className="groups-list-panel">
            <div className="controls-bar glass">
              <div className="search-box">
                <Search size={18} />
                <input 
                  type="text" 
                  placeholder="Buscar por nome ou código..." 
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <div className="filter-box">
                <Filter size={18} />
                <select value={filter} onChange={(e) => setFilter(e.target.value)}>
                  <option value="all">Todos os grupos</option>
                  <option value="owner">Grupos que eu criei</option>
                  <option value="member">Grupos que participo</option>
                  <option value="drawn">Já sorteados</option>
                  <option value="pending">Aguardando sorteio</option>
                </select>
              </div>
            </div>

            {filteredGroups.length === 0 ? (
              <motion.div 
                className="empty-state glass"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
              >
                <div className="empty-icon">🎄</div>
                <h3>Nenhum grupo encontrado</h3>
                <p>Tente mudar o filtro ou use as ações rápidas acima.</p>
              </motion.div>
            ) : (
              <div className="groups-grid">
                {filteredGroups.map((group, index) => (
                  <motion.article 
                    key={group.id} 
                    className="group-manage-card glass"
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.05 }}
                    onClick={() => navigate(`/groups/${group.id}`)}
                    whileHover={{ translateY: -5, backgroundColor: 'rgba(255, 255, 255, 0.04)' }}
                  >
                    <div className="card-badge-row">
                      <span className={`status-pill ${group.dataSorteio ? 'drawn' : 'pending'}`}>
                        {group.dataSorteio ? 'Sorteado' : 'Pendente'}
                      </span>
                      {group.dono?.id === user?.id && <span className="role-pill">Dono</span>}
                    </div>
                    
                    <div className="card-main-content">
                      <h3>{group.nome}</h3>
                      <div className="info-row">
                        <Hash size={14} /> <code>{group.codigoUnico}</code>
                      </div>
                      <div className="info-row">
                        <Calendar size={14} /> 
                        <span>{group.dataEvento ? new Date(group.dataEvento).toLocaleDateString() : 'Não definida'}</span>
                      </div>
                    </div>

                    <div className="card-stats-row">
                      <div className="stat">
                        <Users size={14} />
                        <span>{group.membros?.length || 0} membros</span>
                      </div>
                      <div className="stat">
                        <Clock size={14} />
                        <span>{getDaysUntilEvent(group.dataEvento) ?? '?'} dias</span>
                      </div>
                    </div>

                    <div className="card-action">
                      <span>Gerenciar Grupo</span>
                      <ArrowRight size={16} />
                    </div>
                  </motion.article>
                ))}
              </div>
            )}
          </section>
        </div>
      </main>

      {/* Modais de Criar e Entrar */}
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
              <button type="submit" className="btn-primary" disabled={actionLoading}>
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

export default MyGroups;
