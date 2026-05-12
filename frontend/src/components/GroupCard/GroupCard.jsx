import React, { useState } from 'react';
import './GroupCard.css';

const GroupCard = ({ group, onDraw }) => {
  const [copySuccess, setCopySuccess] = useState(false);

  const copyToClipboard = () => {
    navigator.clipboard.writeText(group.code);
    setCopySuccess(true);
    setTimeout(() => setCopySuccess(false), 2000);
  };

  const getStatusClass = (status) => {
    switch (status.toLowerCase()) {
      case 'aberto': return 'status-aberto';
      case 'sorteado': return 'status-sorteado';
      case 'finalizado': return 'status-finalizado';
      default: return '';
    }
  };

  return (
    <div className="group-card fade-in">
      <div className="group-card-header">
        <div className={`status-badge ${getStatusClass(group.status)}`}>
          {group.status}
        </div>
        <div className="group-actions-top">
          <button className="icon-btn-sm" title="Configurações">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
          </button>
        </div>
      </div>

      <div className="group-card-body">
        <h3 className="group-name-title">{group.name}</h3>
        <p className="group-theme-text">Tema: <span>{group.theme || 'Nenhum'}</span></p>
        
        <div className="invite-code-section">
          <span className="code-label">CÓDIGO CONVITE</span>
          <div className="code-badge-container">
            <span className="code-value">{group.code}</span>
            <button 
              className={`copy-btn ${copySuccess ? 'success' : ''}`} 
              onClick={copyToClipboard}
              title="Copiar código"
            >
              {copySuccess ? (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
              ) : (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>
              )}
            </button>
          </div>
        </div>

        <div className="group-meta-info">
          <div className="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
            <span>Sorteio: {group.drawDate}</span>
          </div>
          <div className="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"></path><path d="M2 17l10 5 10-5"></path><path d="M2 12l10 5 10-5"></path></svg>
            <span>R$ {group.minValue} - R$ {group.maxValue}</span>
          </div>
        </div>

        <div className="participants-avatars">
          {group.participants.slice(0, 3).map((p, i) => (
            <div key={i} className="avatar-mini" title={p.name}>
              <img src={`https://ui-avatars.com/api/?name=${p.name}&background=random&color=fff`} alt={p.name} />
            </div>
          ))}
          {group.participants.length > 3 && (
            <div className="avatar-more">+{group.participants.length - 3}</div>
          )}
          <span className="participants-count">{group.participants.length} participantes</span>
        </div>
      </div>

      <div className="group-card-footer">
        <button className="btn-secondary-sm">Ver Grupo</button>
        {group.status.toLowerCase() === 'aberto' && group.isAdmin && (
          <button className="btn-primary-sm" onClick={() => onDraw(group)}>
            Iniciar Sorteio
          </button>
        )}
      </div>
    </div>
  );
};

export default GroupCard;
