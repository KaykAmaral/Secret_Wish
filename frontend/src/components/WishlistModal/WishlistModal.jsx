import { useState, useEffect, useCallback } from 'react';
import { ExternalLink, Pencil, Trash2 } from 'lucide-react';
import wishlistService from '../../services/wishlistService';
import './WishlistModal.css';

const MAX_WISHLIST_ITEMS = 10;

const WishlistModal = ({ isOpen, onClose }) => {
  const [wishlist, setWishlist] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [editingItem, setEditingItem] = useState(null);

  const [nomeProduto, setNomeProduto] = useState('');
  const [link, setLink] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [alertType, setAlertType] = useState('error');

  // Busca a wishlist atualizada sempre que o modal abre ou uma operacao altera itens.
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
      // Cada abertura comeca no modo lista para evitar manter formulario antigo na tela.
      setIsAdding(false);
      setEditingItem(null);
      setError('');
      setAlertType('error');
      setActionLoading(false);
      const timer = setTimeout(() => {
        fetchWishlist();
      }, 0);
      return () => clearTimeout(timer);
    }
  }, [isOpen, fetchWishlist]);

  // Limpa o formulario de criacao/edicao e volta para a lista.
  const resetForm = () => {
    setNomeProduto('');
    setLink('');
    setEditingItem(null);
    setIsAdding(false);
    setError('');
    setAlertType('error');
  };

  // Inicia criacao garantindo que nenhum item anterior permaneça em edicao.
  const handleStartAdd = () => {
    if ((wishlist?.itens?.length || 0) >= MAX_WISHLIST_ITEMS) {
      setAlertType('warning');
      setError('Sua lista de desejos ja possui o limite de 10 itens.');
      return;
    }

    resetForm();
    setIsAdding(true);
  };

  // Preenche o formulario com os dados do item selecionado para edicao.
  const handleStartEdit = (item) => {
    setEditingItem(item);
    setNomeProduto(item.nomeProduto);
    setLink(item.link || '');
    setIsAdding(true);
    setError('');
    setAlertType('error');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    // Usa validacao propria para exibir alerta visual consistente com o restante da app.
    if (!nomeProduto.trim() || !link.trim()) {
      setAlertType('warning');
      setError('Preencha nome do produto e link.');
      return;
    }

    if (!editingItem && (wishlist?.itens?.length || 0) >= MAX_WISHLIST_ITEMS) {
      setAlertType('warning');
      setError('Sua lista de desejos ja possui o limite de 10 itens.');
      return;
    }

    setActionLoading(true);
    setAlertType('error');
    setError('');
    try {
      const payload = {
        nomeProduto: nomeProduto.trim(),
        link: link.trim(),
      };

      if (editingItem) {
        // O mesmo formulario atende criacao e edicao para manter as regras centralizadas.
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

  // Remove item somente apos confirmacao explicita por ser uma acao destrutiva.
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
                  placeholder="https://amazon.com/..."
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
