import { useState, useEffect, useCallback } from 'react';
import { ExternalLink, Pencil, Trash2 } from 'lucide-react';
import wishlistService from '../../services/wishlistService';
import './WishlistModal.css';

const WishlistModal = ({ isOpen, onClose }) => {
  const [wishlist, setWishlist] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [editingItem, setEditingItem] = useState(null);

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
      setIsAdding(false);
      setEditingItem(null);
      setError('');
      setActionLoading(false);
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
    if (!nomeProduto.trim() || !link.trim()) {
      setError('Preencha nome do produto e link.');
      return;
    }

    setActionLoading(true);
    setError('');
    try {
      const payload = {
        nomeProduto: nomeProduto.trim(),
        link: link.trim(),
      };

      if (editingItem) {
        await wishlistService.updateItem(editingItem.id, payload);
      } else {
        await wishlistService.addItem(payload);
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
    <div className="modal-overlay wishlist-overlay">
      <div className="modal-card glass wishlist-modal">
        <div className="modal-header">
          <h2>Minha Lista de Desejos</h2>
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
                <label>Link</label>
                <input
                  type="url"
                  placeholder="https://amazon.com/..."
                  value={link}
                  onChange={(e) => setLink(e.target.value)}
                  required
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
                <span>+</span> Novo Presente
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
                      <p>Sua lista de desejos está vazia</p>
                    </div>
                  ) : (
                    wishlist?.itens?.map(item => (
                      <div key={item.id} className="wish-item-row">
                        <div className="wish-item-info">
                          <h4>{item.nomeProduto}</h4>
                          {item.link && (
                            <a href={item.link} target="_blank" rel="noopener noreferrer">
                              <ExternalLink size={14} /> Link
                            </a>
                          )}
                        </div>
                        <div className="wish-item-actions">
                          <button onClick={() => handleStartEdit(item)} title="Editar item" aria-label="Editar item">
                            <Pencil size={16} />
                          </button>
                          <button className="delete" onClick={() => handleDelete(item.id)} title="Remover item" aria-label="Remover item">
                            <Trash2 size={16} />
                          </button>
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
