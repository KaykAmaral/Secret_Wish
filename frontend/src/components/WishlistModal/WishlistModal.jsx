import { useState, useEffect, useCallback } from 'react';
import wishlistService from '../../services/wishlistService';
import './WishlistModal.css';

const WishlistModal = ({ isOpen, onClose }) => {
  const [wishlist, setWishlist] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  
  // Form states
  const [nomeProduto, setNomeProduto] = useState('');
  const [link, setLink] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchWishlist = useCallback(async () => {
    try {
      setLoading(true);
      const data = await wishlistService.getMyWishlist();
      setWishlist(data);
    } catch (err) {
      console.error('Erro ao carregar wishlist:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (isOpen) {
      const timer = setTimeout(() => {
        fetchWishlist();
      }, 0);
      return () => clearTimeout(timer);
    }
  }, [isOpen, fetchWishlist]);

  const resetForm = () => {
    setNomeProduto('');
    setLink('');
    setEditingItem(null);
    setIsAdding(false);
    setError('');
  };

  const handleStartAdd = () => {
    resetForm();
    setIsAdding(true);
  };

  const handleStartEdit = (item) => {
    setEditingItem(item);
    setNomeProduto(item.nomeProduto);
    setLink(item.link || '');
    setIsAdding(true);
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    setError('');
    try {
      if (editingItem) {
        await wishlistService.updateItem(editingItem.id, { nomeProduto, link });
      } else {
        await wishlistService.addItem({ nomeProduto, link });
      }
      resetForm();
      fetchWishlist();
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao salvar item.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (itemId) => {
    if (!window.confirm('Tem certeza que deseja remover este item?')) return;
    try {
      await wishlistService.removeItem(itemId);
      fetchWishlist();
    } catch (err) {
      console.error('Erro ao remover item:', err);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-card glass wishlist-modal">
        <div className="modal-header">
          <h2>Minha Lista de Desejos ♥</h2>
          <button className="close-btn" onClick={onClose}>&times;</button>
        </div>

        <div className="wishlist-modal-body">
          {isAdding ? (
            <form onSubmit={handleSubmit} className="modal-form animate-in">
              <h3>{editingItem ? 'Editar Presente' : 'Novo Presente'}</h3>
              <div className="input-group">
                <label>Nome do Produto</label>
                <input 
                  type="text" 
                  placeholder="Ex: Caneca Gamer" 
                  value={nomeProduto}
                  onChange={(e) => setNomeProduto(e.target.value)}
                  required 
                  autoFocus
                />
              </div>
              <div className="input-group">
                <label>Link (Opcional)</label>
                <input 
                  type="url" 
                  placeholder="https://amazon.com/..." 
                  value={link}
                  onChange={(e) => setLink(e.target.value)}
                />
              </div>
              {error && <p className="error-msg">{error}</p>}
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={resetForm}>Cancelar</button>
                <button type="submit" className="btn-primary" disabled={actionLoading}>
                  {actionLoading ? 'Salvando...' : 'Salvar'}
                </button>
              </div>
            </form>
          ) : (
            <div className="wishlist-content animate-in">
              <button className="btn-add-item" onClick={handleStartAdd}>
                <span>+</span> Adicionar Presente
              </button>

              {loading ? (
                <div className="loading-state">
                  <div className="spinner-small"></div>
                  <p>Carregando desejos...</p>
                </div>
              ) : (
                <div className="wish-list-compact">
                  {wishlist?.itens?.length === 0 ? (
                    <div className="empty-wishlist">
                      <p>Sua lista está vazia. Comece a adicionar o que você quer ganhar!</p>
                    </div>
                  ) : (
                    wishlist?.itens?.map(item => (
                      <div key={item.id} className="wish-item-row">
                        <div className="wish-item-info">
                          <h4>{item.nomeProduto}</h4>
                          {item.link && <a href={item.link} target="_blank" rel="noopener noreferrer">🔗 Link</a>}
                        </div>
                        <div className="wish-item-actions">
                          <button onClick={() => handleStartEdit(item)}>✏️</button>
                          <button className="delete" onClick={() => handleDelete(item.id)}>🗑️</button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default WishlistModal;
