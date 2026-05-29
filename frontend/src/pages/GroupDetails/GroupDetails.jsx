import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  ArrowLeft,
  Calendar,
  Clock,
  Crown,
  Gift,
  Hash,
  HelpCircle,
  LogOut,
  Sparkles,
  Trash2,
  User,
  Users,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import groupService from '../../services/groupService';
import drawService from '../../services/drawService';
import messageService from '../../services/messageService';
import webSocketService from '../../services/websocketService';
import PremiumChatDrawer from '../../components/PremiumChatDrawer/PremiumChatDrawer';
import ConfirmationModal from '../../components/ConfirmationModal/ConfirmationModal';
import './GroupDetails.css';

/**
 * Página de Detalhes do Grupo.
 * 
 * Este é o componente mais complexo do frontend. Ele gerencia:
 * 1. Visualização de membros e dados do grupo.
 * 2. Realização do sorteio (restrito ao dono).
 * 3. Abertura de chats anônimos (Amigo Tirado vs Amigo que me tirou).
 * 4. Sincronização em tempo real via WebSockets.
 */
const GroupDetails = () => {
  const { groupId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  // Estados de Dados do Grupo
  const [group, setGroup] = useState(null);
  const [whoITook, setWhoITook] = useState(null);
  const [gifterChat, setGifterChat] = useState(null);
  const [loading, setLoading] = useState(true);
  
  // Estados de UI
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [isFriendChatOpen, setIsFriendChatOpen] = useState(false);
  const [isGifterChatOpen, setIsGifterChatOpen] = useState(false);

  // Estados para Controle de Modais de Confirmação
  const [showLeaveModal, setShowLeaveModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showDrawModal, setShowDrawModal] = useState(false);

  /**
   * Carrega os detalhes do grupo e, caso o sorteio já tenha ocorrido, 
   * busca dados complementares como quem o usuário tirou e resumos de chat.
   */
  const fetchGroupDetails = useCallback(async () => {
    try {
      setLoading(true);
      const data = await groupService.getGroupById(groupId);
      setGroup(data);

      // Se o sorteio já foi realizado, precisamos carregar as informações do par sorteado.
      if (data.dataSorteio) {
        try {
          // Busca o Amigo Secreto do usuário e sua wishlist.
          const drawData = await drawService.getWhoITook(groupId);
          setWhoITook(drawData);

          // Busca os resumos de chat para identificar quem tirou o usuário (chat anônimo).
          const summaries = await messageService.getChatSummaries(groupId);
          const anonymousGifter = summaries.find(c => c.anonimoParaUsuario);
          setGifterChat(anonymousGifter);
        } catch (e) {
          console.warn('[GroupDetails] Sorteio realizado, mas erro ao buscar relações:', e);
        }
      }
    } catch (err) {
      console.error('[GroupDetails] Erro ao carregar grupo:', err);
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  }, [groupId, navigate]);

  /**
   * Inicializa a página e a conexão WebSocket.
   */
  useEffect(() => {
    fetchGroupDetails();
    webSocketService.connect();

    return () => webSocketService.disconnect();
  }, [fetchGroupDetails]);

  /**
   * Trata o início do fluxo de sorteio.
   */
  const handlePerformDraw = () => setShowDrawModal(true);

  /**
   * Executa o sorteio definitivo após confirmação.
   */
  const confirmPerformDraw = async () => {
    setShowDrawModal(false);
    setActionLoading(true);
    setError('');
    try {
      await drawService.performDraw(groupId);
      // Recarrega tudo para habilitar os cards de resultado e chats.
      fetchGroupDetails();
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao realizar sorteio.');
    } finally {
      setActionLoading(false);
    }
  };

  /**
   * Trata a saída do usuário do grupo.
   */
  const confirmLeaveGroup = async () => {
    setShowLeaveModal(false);
    try {
      await groupService.leaveGroup(groupId);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao sair do grupo.');
    }
  };

  /**
   * Trata a exclusão definitiva do grupo (Dono apenas).
   */
  const confirmDeleteGroup = async () => {
    setShowDeleteModal(false);
    try {
      await groupService.deleteGroup(groupId);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao excluir grupo.');
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <Sparkles className="spinning-icon" size={48} />
        <p>Entrando no clima do mistério...</p>
      </div>
    );
  }

  const isDono = group.dono.id === user.id;

  /**
   * Formata a contagem de dias para o evento.
   */
  const formatDaysUntilEvent = (dateValue) => {
    if (!dateValue) return 'A definir';
    const [year, month, day] = dateValue.split('T')[0].split('-').map(Number);
    const eventDate = new Date(year, month - 1, day);
    const today = new Date();
    const todayOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const days = Math.ceil((eventDate - todayOnly) / (1000 * 60 * 60 * 24));

    if (days < 0) return 'Evento encerrado';
    if (days === 0) return 'É hoje!';
    return `Faltam ${days} dias`;
  };

  return (
    <div className="group-details-page">
      {/* Navegação de Retorno */}
      <nav className="breadcrumb">
        <button onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={18} /> Dashboard
        </button>
      </nav>

      {/* Cabeçalho de Informações do Grupo */}
      <motion.header className="group-detail-header glass" initial={{ opacity: 0, y: -20 }} animate={{ opacity: 1, y: 0 }}>
        <div className="header-main">
          <div className="title-area">
            <span className="status-badge large">
              {group.dataSorteio ? <><Sparkles size={14} /> Sorteado</> : <><Clock size={14} /> Aguardando Sorteio</>}
            </span>
            <h1>{group.nome}</h1>
            {group.descricao && <p className="group-desc">{group.descricao}</p>}
          </div>
          <div className="header-actions">
            {isDono && !group.dataSorteio && (
              <button className="btn-premium" onClick={handlePerformDraw} disabled={actionLoading}>
                <Sparkles size={18} /> {actionLoading ? 'Sorteando...' : 'Realizar Sorteio'}
              </button>
            )}
            {!isDono && !group.dataSorteio && (
              <button className="btn-icon danger" onClick={() => setShowLeaveModal(true)} title="Sair do Grupo">
                <LogOut size={20} />
              </button>
            )}
            {isDono && (
              <button className="btn-danger-text" onClick={() => setShowDeleteModal(true)} title="Excluir Grupo">
                <Trash2 size={20} /> Excluir
              </button>
            )}
          </div>
        </div>

        {/* Estatísticas e Datas */}
        <div className="header-stats">
          <div className="stat-item">
            <span className="stat-icon"><Hash size={18} /></span>
            <div><span className="stat-label">Código</span><span className="stat-value highlight">{group.codigoUnico}</span></div>
          </div>
          <div className="stat-item">
            <span className="stat-icon"><Calendar size={18} /></span>
            <div>
              <span className="stat-label">Evento</span>
              <span className="stat-value">{group.dataEvento ? new Date(group.dataEvento).toLocaleDateString() : 'A definir'}</span>
            </div>
          </div>
          <div className="stat-item">
            <span className="stat-icon"><Users size={18} /></span>
            <div><span className="stat-label">Participantes</span><span className="stat-value">{group.membros.length}</span></div>
          </div>
          <div className="stat-item">
            <span className="stat-icon"><Clock size={18} /></span>
            <div><span className="stat-label">Tempo</span><span className="stat-value">{formatDaysUntilEvent(group.dataEvento)}</span></div>
          </div>
        </div>
      </motion.header>

      {error && <div className="error-alert">{error}</div>}

      {/* Grid de Cards de Ação (Sorteio e Chats) */}
      <div className="group-content-grid">
        {/* Card: Amigo que EU tirei (Wishlist e Chat Revelado) */}
        {group.dataSorteio && whoITook ? (
          <motion.section className="premium-card glass purple" whileHover={{ scale: 1.02 }} onClick={() => setIsFriendChatOpen(true)}>
            <div className="card-top">
              <div className="card-icon"><Gift size={44} /></div>
              <h2 className="card-title">Seu Amigo Secreto</h2>
              <p className="card-subtitle">Você tirou <strong>{whoITook.amigoSecreto.nome}</strong>. Veja a wishlist!</p>
            </div>
            <div className="card-footer">
              <span className="chat-status">{whoITook.wishlist.itens.length} itens na lista</span>
              <button className="btn-premium">Abrir Chat</button>
            </div>
          </motion.section>
        ) : (
          <section className="premium-card glass disabled">
            <div className="card-icon"><Clock size={44} /></div>
            <h2 className="card-title">Aguardando Sorteio</h2>
            <p className="card-subtitle">O mistério começará em breve...</p>
          </section>
        )}

        {/* Card: Amigo que ME tirou (Chat Anônimo) */}
        {group.dataSorteio ? (
          <motion.section className="premium-card glass blue" whileHover={{ scale: 1.02 }} onClick={() => setIsGifterChatOpen(true)}>
            <div className="card-top">
              <div className="card-icon"><HelpCircle size={44} /></div>
              <h2 className="card-title">Quem está te presenteando?</h2>
              <p className="card-subtitle">Alguém misterioso está observando sua wishlist.</p>
            </div>
            <div className="card-footer">
              <span className="chat-status">{gifterChat?.unreadCount > 0 ? `${gifterChat.unreadCount} novas mensagens` : 'Chat ativo'}</span>
              <button className="btn-premium">Responder Anônimo</button>
            </div>
          </motion.section>
        ) : (
          <section className="premium-card glass disabled">
            <div className="card-icon"><HelpCircle size={44} /></div>
            <h2 className="card-title">Quem te tirou?</h2>
            <p className="card-subtitle">Identidade protegida pelo segredo.</p>
          </section>
        )}
      </div>

      {/* Lista de Participantes */}
      <motion.section className="members-section glass" initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.4 }}>
        <div className="section-header">
          <h2>Participantes do Grupo</h2>
          <span>{group.membros.length} pessoas confirmadas</span>
        </div>
        <div className="members-list">
          {group.membros.map(membro => (
            <div key={membro.id} className="member-item">
              <div className="member-avatar">
                {membro.imagemUrl ? <img src={membro.imagemUrl} alt={membro.nome} className="avatar-img" /> : membro.nome.charAt(0).toUpperCase()}
              </div>
              <div className="member-info">
                <span className="member-name">
                  {membro.nome}
                  {membro.id === group.dono.id && <Crown className="owner-icon" size={15} title="Dono do Grupo" />}
                  {membro.id === user.id && <span className="me-tag">(Você)</span>}
                </span>
              </div>
            </div>
          ))}
        </div>
      </motion.section>

      {/* Gavetas de Chat Lateral */}
      {whoITook && (
        <PremiumChatDrawer
          isOpen={isFriendChatOpen}
          onClose={() => setIsFriendChatOpen(false)}
          groupId={groupId}
          otherUserId={whoITook.amigoSecreto.id}
          title={whoITook.amigoSecreto.nome}
          isAnonymousChat={false}
        />
      )}

      {gifterChat && (
        <PremiumChatDrawer
          isOpen={isGifterChatOpen}
          onClose={() => setIsGifterChatOpen(false)}
          groupId={groupId}
          otherUserId={gifterChat.outroUsuarioId}
          title="Amigo Secreto"
          isAnonymousChat={true}
        />
      )}

      {/* Modais de Confirmação */}
      <ConfirmationModal isOpen={showLeaveModal} onClose={() => setShowLeaveModal(false)} onConfirm={confirmLeaveGroup} title="Sair do Grupo?" message="Você deseja sair? Se o sorteio já ocorreu, sua ausência pode afetar o jogo." />
      <ConfirmationModal isOpen={showDeleteModal} onClose={() => setShowDeleteModal(false)} onConfirm={confirmDeleteGroup} title="Excluir Grupo?" message="ATENÇÃO: Esta ação é irreversível e apagará todas as mensagens e sorteios." />
      <ConfirmationModal isOpen={showDrawModal} onClose={() => setShowDrawModal(false)} onConfirm={confirmPerformDraw} title="Realizar Sorteio?" message="Confirmar sorteio? Novos membros não poderão entrar após esta ação." variant="info" />
    </div>
  );
};

export default GroupDetails;
