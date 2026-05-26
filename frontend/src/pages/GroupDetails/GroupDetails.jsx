import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Gift, HelpCircle, Users, Calendar, Hash, ArrowLeft, Trash2, LogOut, Sparkles } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import groupService from '../../services/groupService';
import drawService from '../../services/drawService';
import messageService from '../../services/messageService';
import webSocketService from '../../services/websocketService';
import PremiumChatDrawer from '../../components/PremiumChatDrawer/PremiumChatDrawer';
import './GroupDetails.css';

const GroupDetails = () => {
  const { groupId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [group, setGroup] = useState(null);
  const [whoITook, setWhoITook] = useState(null);
  const [gifterChat, setGifterChat] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [isFriendChatOpen, setIsFriendChatOpen] = useState(false);
  const [isGifterChatOpen, setIsGifterChatOpen] = useState(false);

  const fetchGroupDetails = useCallback(async () => {
    try {
      setLoading(true);
      const data = await groupService.getGroupById(groupId);
      setGroup(data);
      
      if (data.dataSorteio) {
        try {
          const drawData = await drawService.getWhoITook(groupId);
          setWhoITook(drawData);
          
          // Buscar resumos de chat para encontrar quem tirou o usuário
          const summaries = await messageService.getChatSummaries(groupId);
          const anonymousGifter = summaries.find(c => c.anonimoParaUsuario);
          setGifterChat(anonymousGifter);
        } catch (e) {
          console.warn('Sorteio realizado mas erro ao buscar dados adicionais', e);
        }
      }
    } catch (err) {
      console.error('Erro ao buscar detalhes do grupo:', err);
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  }, [groupId, navigate]);

  useEffect(() => {
    fetchGroupDetails();
    // Conectar WebSocket (em ambiente real, o token viria do AuthContext ou seria pego via cookie se o backend suportasse)
    // Para dev, tentamos conectar. O backend com dev-auth habilitado pode precisar do token no header.
    // Como não temos acesso fácil ao token HttpOnly aqui, esperamos que o backend aceite a conexão se o cookie for enviado.
    webSocketService.connect();
    
    return () => webSocketService.disconnect();
  }, [fetchGroupDetails]);

  const handlePerformDraw = async () => {
    if (!window.confirm('Tem certeza que deseja realizar o sorteio agora? Ninguém mais poderá entrar no grupo.')) return;
    
    setActionLoading(true);
    setError('');
    try {
      await drawService.performDraw(groupId);
      fetchGroupDetails();
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao realizar sorteio.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleLeaveGroup = async () => {
    if (!window.confirm('Tem certeza que deseja sair do grupo?')) return;
    try {
      await groupService.leaveGroup(groupId);
      navigate('/dashboard');
    } catch (err) {
      alert(err.response?.data?.message || 'Erro ao sair do grupo.');
    }
  };

  const handleDeleteGroup = async () => {
    if (!window.confirm('⚠️ ATENÇÃO: Esta ação é irreversível.')) return;
    try {
      await groupService.deleteGroup(groupId);
      navigate('/dashboard');
    } catch (err) {
      alert(err.response?.data?.message || 'Erro ao excluir grupo.');
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

  return (
    <div className="group-details-page">
      <nav className="breadcrumb">
        <button onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={18} /> Voltar para o Dashboard
        </button>
      </nav>

      <motion.header 
        className="group-detail-header glass"
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="header-main">
          <div className="title-area">
            <span className="status-badge large">
              {group.dataSorteio ? '✨ Sorteio Realizado' : '⏳ Aguardando Sorteio'}
            </span>
            <h1>{group.nome}</h1>
            {group.descricao && <p className="group-desc">{group.descricao}</p>}
          </div>
          <div className="header-actions">
            {isDono && !group.dataSorteio && (
              <button className="btn-premium" onClick={handlePerformDraw} disabled={actionLoading}>
                {actionLoading ? 'Sorteando...' : '🎲 Realizar Sorteio'}
              </button>
            )}
            {!isDono && !group.dataSorteio && (
              <button className="btn-icon danger" onClick={handleLeaveGroup} title="Sair do Grupo">
                <LogOut size={20} />
              </button>
            )}
            {isDono && (
              <button className="btn-icon danger" onClick={handleDeleteGroup} title="Excluir Grupo">
                <Trash2 size={20} />
              </button>
            )}
          </div>
        </div>

        <div className="header-stats">
          <div className="stat-item">
            <Hash size={18} />
            <div>
              <span className="stat-label">Código</span>
              <span className="stat-value highlight">{group.codigoUnico}</span>
            </div>
          </div>
          <div className="stat-item">
            <Calendar size={18} />
            <div>
              <span className="stat-label">Evento</span>
              <span className="stat-value">{group.dataEvento ? new Date(group.dataEvento).toLocaleDateString() : 'A definir'}</span>
            </div>
          </div>
          <div className="stat-item">
            <Users size={18} />
            <div>
              <span className="stat-label">Membros</span>
              <span className="stat-value">{group.membros.length}</span>
            </div>
          </div>
        </div>
      </motion.header>

      {error && <div className="error-alert">{error}</div>}

      <div className="group-content-grid">
        {/* CARD 1: Seu Amigo Secreto */}
        {group.dataSorteio && whoITook ? (
          <motion.section 
            className="premium-card glass purple"
            whileHover={{ scale: 1.02 }}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            onClick={() => setIsFriendChatOpen(true)}
          >
            <div className="card-top">
              <div className="card-icon">🎁</div>
              <h2 className="card-title">Seu Amigo Secreto</h2>
              <p className="card-subtitle">
                Você tirou <strong>{whoITook.amigoSecreto.nome}</strong>. 
                Veja a wishlist e converse anonimamente!
              </p>
            </div>
            <div className="card-footer">
              <span className="chat-status">
                {whoITook.wishlist.itens.length} itens na lista
              </span>
              <button className="btn-premium">Ver Wishlist e Chat</button>
            </div>
          </motion.section>
        ) : (
          <section className="premium-card glass disabled">
            <div className="card-icon">🔒</div>
            <h2 className="card-title">Aguardando Sorteio</h2>
            <p className="card-subtitle">O mistério começará em breve...</p>
          </section>
        )}

        {/* CARD 2: Quem está te presenteando? */}
        {group.dataSorteio ? (
          <motion.section 
            className="premium-card glass blue"
            whileHover={{ scale: 1.02 }}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            onClick={() => setIsGifterChatOpen(true)}
          >
            <div className="card-top">
              <div className="card-icon"><HelpCircle size={48} /></div>
              <h2 className="card-title">Quem está te presenteando?</h2>
              <p className="card-subtitle">
                👀 Alguém misterioso está observando sua wishlist. 
                Envie uma mensagem para o seu amigo secreto!
              </p>
            </div>
            <div className="card-footer">
              <span className="chat-status">
                {gifterChat?.unreadCount > 0 ? `💬 ${gifterChat.unreadCount} novas mensagens` : 'Chat ativo'}
              </span>
              <button className="btn-premium">Abrir Chat Anônimo</button>
            </div>
          </motion.section>
        ) : (
          <section className="premium-card glass disabled">
            <div className="card-icon">👀</div>
            <h2 className="card-title">Quem te tirou?</h2>
            <p className="card-subtitle">Você saberá quem é (ou não) em breve.</p>
          </section>
        )}
      </div>

      {/* Seção de Membros */}
      <motion.section 
        className="members-section glass mt-4"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.4 }}
      >
        <div className="section-header">
          <h2>Participantes do Grupo</h2>
          <span>{group.membros.length} pessoas confirmadas</span>
        </div>
        <div className="members-list">
          {group.membros.map(membro => (
            <div key={membro.id} className="member-item">
              <div className="member-avatar">{membro.nome.charAt(0)}</div>
              <div className="member-info">
                <span className="member-name">
                  {membro.nome} 
                  {membro.id === group.dono.id && <span className="owner-tag">👑</span>}
                  {membro.id === user.id && <span className="me-tag">(Você)</span>}
                </span>
              </div>
            </div>
          ))}
        </div>
      </motion.section>

      {/* Drawers */}
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
          title="Seu Amigo Secreto"
          isAnonymousChat={true}
        />
      )}
    </div>
  );
};

export default GroupDetails;
