import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import groupService from '../../services/groupService';
import drawService from '../../services/drawService';
import './GroupDetails.css';

const GroupDetails = () => {
  const { groupId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [group, setGroup] = useState(null);
  const [whoITook, setWhoITook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchGroupDetails = useCallback(async () => {
    try {
      setLoading(true);
      const data = await groupService.getGroupById(groupId);
      setGroup(data);
      
      if (data.dataSorteio) {
        try {
          const drawData = await drawService.getWhoITook(groupId);
          setWhoITook(drawData);
        } catch {
          console.warn('Sorteio realizado mas erro ao buscar destinatário');
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
    const timer = setTimeout(() => {
      fetchGroupDetails();
    }, 0);
    return () => clearTimeout(timer);
  }, [fetchGroupDetails]);

  const handlePerformDraw = async () => {
    if (!window.confirm('Tem certeza que deseja realizar o sorteio agora? Ninguém mais poderá entrar no grupo.')) return;
    
    setActionLoading(true);
    setError('');
    try {
      await drawService.performDraw(groupId);
      fetchGroupDetails();
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao realizar sorteio. Verifique se o grupo tem participantes suficientes.');
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
    if (!window.confirm('⚠️ ATENÇÃO: Esta ação é irreversível. Excluir o grupo apagará todos os dados e sorteios. Confirmar?')) return;
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
        <div className="spinner"></div>
        <p>Carregando detalhes do grupo...</p>
      </div>
    );
  }

  const isDono = group.dono.id === user.id;

  return (
    <div className="group-details-page">
      <nav className="breadcrumb">
        <button onClick={() => navigate('/dashboard')}>← Voltar para o Dashboard</button>
      </nav>

      <header className="group-detail-header glass">
        <div className="header-main">
          <div className="title-area">
            <span className="status-badge large">{group.dataSorteio ? 'Sorteado' : 'Em Aberto'}</span>
            <h1>{group.nome}</h1>
            {group.descricao && <p className="group-desc">{group.descricao}</p>}
          </div>
          <div className="header-actions">
            {isDono && !group.dataSorteio && (
              <button className="btn-draw" onClick={handlePerformDraw} disabled={actionLoading}>
                {actionLoading ? 'Sorteando...' : '🎲 Realizar Sorteio'}
              </button>
            )}
            {!isDono && !group.dataSorteio && (
              <button className="btn-outline-danger" onClick={handleLeaveGroup}>Sair do Grupo</button>
            )}
            {isDono && (
              <button className="btn-icon delete" onClick={handleDeleteGroup} title="Excluir Grupo">🗑️</button>
            )}
          </div>
        </div>

        <div className="header-stats">
          <div className="stat-item">
            <span className="stat-label">Código Único</span>
            <span className="stat-value highlight">{group.codigoUnico}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Data do Evento</span>
            <span className="stat-value">{group.dataEvento ? new Date(group.dataEvento).toLocaleDateString() : 'A definir'}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Participantes</span>
            <span className="stat-value">{group.membros.length}</span>
          </div>
        </div>
      </header>

      {error && <div className="error-alert">{error}</div>}

      <div className="group-content-grid">
        {/* Resultado do Sorteio (se houver) */}
        {group.dataSorteio && whoITook && (
          <section className="draw-result-card glass highlight-border">
            <div className="result-header">
              <h2>Seu Amigo Secreto 🕵️</h2>
            </div>
            <div className="result-body">
              <div className="friend-info">
                <div className="avatar-large">{whoITook.amigoSecreto.nome.charAt(0)}</div>
                <div className="friend-text">
                  <h3>{whoITook.amigoSecreto.nome}</h3>
                  <p>{whoITook.amigoSecreto.email}</p>
                </div>
              </div>
              <div className="result-actions">
                <button className="btn-primary" onClick={() => alert('Funcionalidade de Chat Anônimo em desenvolvimento!')}>
                  Ver Wishlist e Chat
                </button>
              </div>
            </div>
          </section>
        )}

        {/* Lista de Membros */}
        <section className="members-section glass">
          <div className="section-header">
            <h2>Participantes</h2>
            <span>{group.membros.length} pessoas</span>
          </div>
          <div className="members-list">
            {group.membros.map(membro => (
              <div key={membro.id} className="member-item">
                <div className="member-avatar">{membro.nome.charAt(0)}</div>
                <div className="member-info">
                  <span className="member-name">{membro.nome} {membro.id === group.dono.id && <span className="owner-tag">(Dono)</span>}</span>
                  <span className="member-email">{membro.email}</span>
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
};

export default GroupDetails;
