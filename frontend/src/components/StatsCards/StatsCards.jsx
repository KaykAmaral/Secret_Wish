import React from 'react';
import './StatsCards.css';

const StatsCards = () => {
  const stats = [
    { label: 'Total de Grupos', value: '08', icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
    ), color: 'blue' },
    { label: 'Sorteios Ativos', value: '03', icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>
    ), color: 'purple' },
    { label: 'Grupos Liderando', value: '05', icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"></path></svg>
    ), color: 'orange' },
  ];

  return (
    <div className="stats-cards-container">
      {stats.map((stat, index) => (
        <div key={index} className="stat-card-mini fade-in" style={{ animationDelay: `${index * 0.1}s` }}>
          <div className={`stat-icon-mini ${stat.color}`}>
            {stat.icon}
          </div>
          <div className="stat-content-mini">
            <span className="stat-value-mini">{stat.value}</span>
            <span className="stat-label-mini">{stat.label}</span>
          </div>
        </div>
      ))}
    </div>
  );
};

export default StatsCards;
