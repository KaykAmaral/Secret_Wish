import React from 'react';
import './EmptyState.css';

const EmptyState = ({ onCreateClick }) => {
  return (
    <div className="empty-state-container fade-in">
      <div className="empty-state-illustration">
        <svg width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round" className="empty-icon"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
      </div>
      <h3 className="empty-title">Nenhum grupo encontrado</h3>
      <p className="empty-desc">Você ainda não participa de nenhum grupo de amigo secreto. Crie um novo grupo ou peça o código de convite para seus amigos!</p>
      <button className="btn-primary-large" onClick={onCreateClick}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
        Criar Meu Primeiro Grupo
      </button>
    </div>
  );
};

export default EmptyState;
