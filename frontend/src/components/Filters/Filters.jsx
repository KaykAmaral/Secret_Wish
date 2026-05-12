import React from 'react';
import './Filters.css';

const Filters = ({ activeFilter, onFilterChange }) => {
  const filters = ['Todos', 'Aberto', 'Sorteado', 'Finalizado'];

  return (
    <div className="filters-container">
      {/* Desktop Filters */}
      <div className="filters-desktop">
        {filters.map((filter) => (
          <button
            key={filter}
            className={`filter-btn ${activeFilter === filter ? 'active' : ''}`}
            onClick={() => onFilterChange(filter)}
          >
            {filter}
          </button>
        ))}
      </div>

      {/* Mobile Filter Dropdown */}
      <div className="filters-mobile">
        <select 
          value={activeFilter} 
          onChange={(e) => onFilterChange(e.target.value)}
          className="filter-select"
        >
          {filters.map((filter) => (
            <option key={filter} value={filter}>{filter}</option>
          ))}
        </select>
      </div>
    </div>
  );
};

export default Filters;
