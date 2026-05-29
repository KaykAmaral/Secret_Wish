import { useState, useEffect, useCallback } from 'react';
import { ExternalLink, Pencil, Trash2 } from 'lucide-react';
import ConfirmationModal from '../ConfirmationModal/ConfirmationModal';
import wishlistService from '../../services/wishlistService';
import './WishlistModal.css';

const MAX_WISHLIST_ITEMS = 10;

/**
 * Modal de Gerenciamento de Wishlist.
 * 
 * Uma versão compacta e em modal da página de Wishlist, permitindo que o usuário
 * atualize seus desejos sem sair do contexto atual (ex: Dashboard ou GroupDetails).
 */
const WishlistModal = ({ isOpen, onClose }) => {
  const [wishlist, setWishlist] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [editingItem, setEditingItem] = useState(null);

  const [nomeProduto, setNomeProduto] = useState('');
  const [link, setLink] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [error, setError] = useState('');
  const [alertType, setAlertType] = useState('error');

  /**
   * Busca os itens da wishlist do usuário.
   */
  const fetchWishlist = useCallback(async () => {
    try {
      setLoading(true);
      const data = await wishlistService.getMyWishlist();
      setWishlist(data);
    } catch (err) {
      console.error('[WishlistModal] Erro ao carregar dados:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Efeito para carregar dados e resetar estados sempre que o modal é aberto.
   */
  useEffect(() => {
    if (isOpen) {
      setIsAdding(false);
      setEditingItem(null);
      setError('');
      setAlertType('error');
      setActionLoading(false);
      setItemToDelete(null);
      fetchWishlist();
    }
  }, [isOpen, fetchWishlist]);

  /**
   * Reseta o formulário interno.
   */
  const resetForm = () => {
    setNomeProduto('');
    setLink('');
    setEditingItem(null);
    setIsAdding(false);
    setError('');
    setAlertType('error');
  };

  /**
   * Inicia o fluxo de adição de item.
   */
  const handleStartAdd = () => {
    if ((wishlist?.itens?.length || 0) >= MAX_WISHLIST_ITEMS) {
      setAlertType('warning');
      setError('Sua lista já possui o limite de 10 itens.');
      return;
    }
    resetForm();
    setIsAdding(true);
  };

  /**
   * Inicia o fluxo de edição de um item.
   */
  const handleStartEdit = (item) => {
    setEditingItem(item);
    setNomeProduto(item.nomeProduto);
    setLink(item.link || '');
    setIsAdding(true);
    setError('');
    setAlertType('error');
  };

  /**
   * Trata o salvamento das alterações.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!nomeProduto.trim() || !link.trim()) {
      setAlertType('warning');
      setError('Preencha o nome do produto e o link.');
      return;
    }

    if (!editingItem && (wishlist?.itens?.length || 0) >= MAX_WISHLIST_ITEMS) {
      setAlertType('warning');
      setError('Sua lista já possui o limite de 10 itens.');
      return;
    }

    setActionLoading(true);
    setAlertType('error');
    setError('');
    try {
      const payload = { nomeProduto: nomeProduto.trim(), link: link.trim() };

      if (editingItem) {
        await wishlistService.updateItem(editingItem.id, payload);
      } else {
        await wishlistService.addItem(payload);
      }
      resetForm();
      fetchWishlist();
    } catch (err) {
      setAlertType('error');
      setError(err.response?.data?.message || 'Erro ao salvar item.');
    } finally {
      setActionLoading(false);
    }
  };

  /**
   * Prepara a exclusão de um item.
   */
  const handleDelete = (itemId) => {
    setItemToDelete(itemId);
  };

  /**
   * Confirma a exclusão definitiva.
   */
  const confirmDelete = async () => {
    if (!itemToDelete) return;
    try {
      await wishlistService.removeItem(itemToDelete);
      setItemToDelete(null);
      fetchWishlist();
    } catch (err) {
      console.error('[WishlistModal] Erro ao remover item:', err);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay wishlist-overlay">
      <div className="modal-card glass wishlist-modal">
        <div className="modal-header">
          <h2>Minha Lista de Desejos</h2>
          <button className="close-btn" onClick={onClose} aria-label="Fechar">&times;</button>
        </div>

        <div className="wishlist-modal-body">
          {isAdding ? (
            <form onSubmit={handleSubmit} className="modal-form animate-in" noValidate>
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
                  placeholder="https://..."
                  value={link}
                  onChange={(e) => setLink(e.target.value)}
                  required
                />
              </div>
              {error && <p className={`form-alert ${alertType}`}>{error}</p>}
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
              {error && <p className={`form-alert ${alertType}`}>{error}</p>}

              {loading ? (
                <div className="loading-state">
                  <div className="spinner-small"></div>
                  <p>Carregando desejos...</p>
                </div>
              ) : (
                <div className="wish-list-compact">
                  {wishlist?.itens?.length === 0 ? (
                    <div className="empty-wishlist">
                      <p>Sua lista de desejos está vazia.</p>
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
                          <button onClick={() => handleStartEdit(item)} title="Editar item">
                            <Pencil size={16} />
                          </button>
                          <button className="delete" onClick={() => handleDelete(item.id)} title="Remover item">
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
      <ConfirmationModal
        isOpen={Boolean(itemToDelete)}
        onClose={() => setItemToDelete(null)}
        onConfirm={confirmDelete}
        title="Remover item?"
        message="Tem certeza que deseja remover este item?"
        confirmText="Remover"
        variant="danger"
      />
    </div>
  );
};

export default WishlistModal;
