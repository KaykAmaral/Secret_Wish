import React from 'react';
import './Dashboard.css';

const Dashboard = () => {
  const cards = [
    { title: 'Total de Grupos', value: '12', description: 'Grupos que você participa', icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
    ), color: 'blue' },
    { title: 'Sorteios Ativos', value: '04', description: 'Aguardando revelação', icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>
    ), color: 'purple' },
    { title: 'Mensagens', value: '08', description: 'Novas mensagens no chat', icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
    ), color: 'green' },
    { title: 'Convites', value: '03', description: 'Pendentes de aprovação', icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
    ), color: 'orange' },
  ];

  const recentGroups = [
    { id: 1, name: 'Família Silva 2026', members: 15, status: 'Sorteado', date: '24 Dez 2026' },
    { id: 2, name: 'Amigos do Trabalho', members: 8, status: 'Aberto', date: '15 Dez 2026' },
    { id: 3, name: 'Galera da Faculdade', members: 12, status: 'Aberto', date: '20 Dez 2026' },
    { id: 4, name: 'Vizinhos do Prédio', members: 6, status: 'Sorteado', date: '18 Dez 2026' },
  ];

  const activities = [
    { id: 1, user: 'Mariana', action: 'entrou no grupo', target: 'Amigos do Trabalho', time: 'Há 5 minutos' },
    { id: 2, user: 'Sistema', action: 'realizou o sorteio em', target: 'Família Silva 2026', time: 'Há 2 horas' },
    { id: 3, user: 'Carlos', action: 'enviou uma mensagem anônima', target: '', time: 'Há 3 horas' },
    { id: 4, user: 'Você', action: 'criou o grupo', target: 'Galera da Faculdade', time: 'Ontem' },
  ];

  return (
    <div className="dashboard-container fade-in">
      <div className="dashboard-header">
        <h1 className="dashboard-title">Olá, Kayky! 👋</h1>
        <p className="dashboard-subtitle">Acompanhe seus sorteios e grupos de amigo secreto.</p>
      </div>
      
      <div className="stats-grid">
        {cards.map((card, index) => (
          <div key={index} className="stat-card">
            <div className={`stat-icon ${card.color}`}>
              {card.icon}
            </div>
            <div className="stat-info">
              <span className="stat-value">{card.value}</span>
              <span className="stat-label">{card.title}</span>
              <span className="stat-desc">{card.description}</span>
            </div>
          </div>
        ))}
      </div>
      
      <div className="dashboard-grid">
        <section className="dashboard-section recent-groups">
          <div className="section-header">
            <h2 className="section-title">Grupos Recentes</h2>
            <button className="view-all">Ver todos</button>
          </div>
          <div className="groups-list">
            {recentGroups.map((group) => (
              <div key={group.id} className="group-item">
                <div className="group-info">
                  <div className="group-avatar">{group.name.charAt(0)}</div>
                  <div className="group-details">
                    <span className="group-name">{group.name}</span>
                    <span className="group-meta">{group.members} membros • {group.date}</span>
                  </div>
                </div>
                <div className={`group-status ${group.status.toLowerCase()}`}>
                  {group.status}
                </div>
              </div>
            ))}
          </div>
        </section>
        
        <section className="dashboard-section recent-activities">
          <div className="section-header">
            <h2 className="section-title">Atividades Recentes</h2>
          </div>
          <div className="activities-list">
            {activities.map((activity) => (
              <div key={activity.id} className="activity-item">
                <div className="activity-dot"></div>
                <div className="activity-content">
                  <p>
                    <strong>{activity.user}</strong> {activity.action} {activity.target && <strong>{activity.target}</strong>}
                  </p>
                  <span className="activity-time">{activity.time}</span>
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
};

export default Dashboard;
