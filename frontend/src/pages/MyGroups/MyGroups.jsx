import React, { useState, useMemo } from 'react';
import StatsCards from '../../components/StatsCards/StatsCards';
import SearchBar from '../../components/SearchBar/SearchBar';
import Filters from '../../components/Filters/Filters';
import GroupCard from '../../components/GroupCard/GroupCard';
import CreateGroupModal from '../../components/CreateGroupModal/CreateGroupModal';
import EmptyState from '../../components/EmptyState/EmptyState';
import './MyGroups.css';

const MOCK_GROUPS = [
  { 
    id: 1, 
    name: 'Família Silva 2026', 
    code: 'AB12-CD34', 
    theme: 'Natal', 
    status: 'Sorteado', 
    drawDate: '24/12/2026', 
    minValue: '50', 
    maxValue: '150',
    participants: [{name: 'João'}, {name: 'Maria'}, {name: 'Carlos'}, {name: 'Ana'}],
    isAdmin: true
  },
  { 
    id: 2, 
    name: 'Amigos do Trabalho', 
    code: 'X7Y9-Z8K2', 
    theme: 'Eletrônicos', 
    status: 'Aberto', 
    drawDate: '15/12/2026', 
    minValue: '100', 
    maxValue: '300',
    participants: [{name: 'Pedro'}, {name: 'Juliana'}, {name: 'Marcos'}],
    isAdmin: false
  },
  { 
    id: 3, 
    name: 'Galera da Faculdade', 
    code: 'QWER-TY12', 
    theme: 'Livros', 
    status: 'Aberto', 
    drawDate: '20/12/2026', 
    minValue: '30', 
    maxValue: '60',
    participants: [{name: 'Lucas'}, {name: 'Fernanda'}, {name: 'Gabriel'}, {name: 'Beatriz'}, {name: 'Igor'}],
    isAdmin: true
  },
];

const MyGroups = () => {
  const [groups, setGroups] = useState(MOCK_GROUPS);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeFilter, setActiveFilter] = useState('Todos');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const filteredGroups = useMemo(() => {
    return groups.filter(group => {
      const matchesSearch = group.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
                            group.code.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesFilter = activeFilter === 'Todos' || group.status === activeFilter;
      return matchesSearch && matchesFilter;
    });
  }, [groups, searchTerm, activeFilter]);

  const handleCreateGroup = (formData) => {
    // Simulação de criação (no futuro chamará o backend)
    const newGroup = {
      id: groups.length + 1,
      name: formData.name,
      code: 'NEW1-CODE', // Backend geraria isso
      theme: formData.theme,
      status: 'Aberto',
      drawDate: formData.drawDate || 'TBD',
      minValue: formData.minValue || '0',
      maxValue: formData.maxValue || '0',
      participants: [{name: 'Você'}],
      isAdmin: true
    };
    setGroups([newGroup, ...groups]);
    setIsModalOpen(false);
    // Aqui poderíamos disparar um Toast de sucesso
  };

  const handlePerformDraw = (group) => {
    if (group.participants.length < 3) {
      alert('O grupo precisa de pelo menos 3 participantes para o sorteio.');
      return;
    }
    
    if (window.confirm(`Deseja realmente iniciar o sorteio do grupo "${group.name}"?`)) {
      setGroups(groups.map(g => g.id === group.id ? {...g, status: 'Sorteado'} : g));
      // No futuro, chamar drawService.performDraw(groupId, donoId)
    }
  };

  return (
    <div className="my-groups-page fade-in">
      <div className="page-header">
        <div className="header-info">
          <h1 className="page-title">Meus Grupos</h1>
          <p className="page-subtitle">Gerencie seus grupos de amigo secreto</p>
        </div>
        <button className="btn-create-group" onClick={() => setIsModalOpen(true)}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          Criar Grupo
        </button>
      </div>

      <StatsCards />

      <div className="search-filter-section">
        <SearchBar onSearch={setSearchTerm} />
        <Filters activeFilter={activeFilter} onFilterChange={setActiveFilter} />
      </div>

      {filteredGroups.length > 0 ? (
        <div className="groups-grid">
          {filteredGroups.map(group => (
            <GroupCard key={group.id} group={group} onDraw={handlePerformDraw} />
          ))}
        </div>
      ) : (
        <EmptyState onCreateClick={() => setIsModalOpen(true)} />
      )}

      <CreateGroupModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onCreate={handleCreateGroup}
      />
    </div>
  );
};

export default MyGroups;
