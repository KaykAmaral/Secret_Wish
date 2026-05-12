import React, { useState } from 'react';
import './CreateGroupModal.css';

const CreateGroupModal = ({ isOpen, onClose, onCreate }) => {
  const [formData, setFormData] = useState({
    name: '',
    theme: '',
    minValue: '',
    maxValue: '',
    drawDate: '',
    isPrivate: true
  });

  const [errors, setErrors] = useState({});

  if (!isOpen) return null;

  const validate = () => {
    const newErrors = {};
    if (!formData.name.trim()) newErrors.name = 'O nome do grupo é obrigatório';
    if (formData.minValue && formData.maxValue && Number(formData.minValue) > Number(formData.maxValue)) {
      newErrors.values = 'O valor mínimo não pode ser maior que o máximo';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      onCreate(formData);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-container fade-in" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">Criar Novo Grupo</h2>
          <button className="close-modal-btn" onClick={onClose}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label>Nome do Grupo *</label>
            <input 
              type="text" 
              placeholder="Ex: Família Silva 2026" 
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              className={errors.name ? 'input-error' : ''}
            />
            {errors.name && <span className="error-text">{errors.name}</span>}
          </div>

          <div className="form-group">
            <label>Tema (Opcional)</label>
            <input 
              type="text" 
              placeholder="Ex: Natal, Eletrônicos, etc." 
              value={formData.theme}
              onChange={(e) => setFormData({...formData, theme: e.target.value})}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Valor Mínimo (R$)</label>
              <input 
                type="number" 
                placeholder="0.00" 
                value={formData.minValue}
                onChange={(e) => setFormData({...formData, minValue: e.target.value})}
              />
            </div>
            <div className="form-group">
              <label>Valor Máximo (R$)</label>
              <input 
                type="number" 
                placeholder="0.00" 
                value={formData.maxValue}
                onChange={(e) => setFormData({...formData, maxValue: e.target.value})}
              />
            </div>
          </div>
          {errors.values && <span className="error-text">{errors.values}</span>}

          <div className="form-group">
            <label>Data do Sorteio</label>
            <input 
              type="date" 
              value={formData.drawDate}
              onChange={(e) => setFormData({...formData, drawDate: e.target.value})}
            />
          </div>

          <div className="form-group">
            <label>Privacidade</label>
            <div className="radio-group">
              <label className="radio-label">
                <input 
                  type="radio" 
                  name="privacy" 
                  checked={formData.isPrivate} 
                  onChange={() => setFormData({...formData, isPrivate: true})} 
                />
                <span>Privado (Somente com código)</span>
              </label>
              <label className="radio-label">
                <input 
                  type="radio" 
                  name="privacy" 
                  checked={!formData.isPrivate} 
                  onChange={() => setFormData({...formData, isPrivate: false})} 
                />
                <span>Público</span>
              </label>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn-cancel" onClick={onClose}>Cancelar</button>
            <button type="submit" className="btn-submit">Criar Grupo</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateGroupModal;
