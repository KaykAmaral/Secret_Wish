import { useCallback, useEffect, useState } from 'react';
import { ArrowLeft, ExternalLink, Pencil, Plus, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import ConfirmationModal from '../../components/ConfirmationModal/ConfirmationModal';
import wishlistService from '../../services/wishlistService';
import './Wishlist.css';

// Limite máximo de itens por wishlist definido pela regra de negócio.
const MAX_WISHLIST_ITEMS = 10;

/**
 * Página de Lista de Desejos (Wishlist).
 * 
 * Permite ao usuário cadastrar produtos que deseja ganhar no Amigo Secreto.
 * A lista é essencial para ajudar o "amigo" que o tirou a escolher o presente ideal.
 */
const Wishlist = () => {
  const navigate = useNavigate();
  
  // Estados de Dados
  const [wishlist, setWishlist] = useState(null);
  const [loading, setLoading] = useState(true);
  
  // Estados de Gerenciamento do Formulário
  const [isAdding, setIsAdding] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [nomeProduto, setNomeProduto] = useState('');
  const [link, setLink] = useState('');
  
  // Estados de Feedback e Ações
  const [actionLoading, setActionLoading] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [error, setError] = useState('');
  const [alertType, setAlertType] = useState('error');

  /**
   * Recupera a wishlist do usuário autenticado.
   */
  const fetchWishlist = useCallback(async () => {
    try {
      setLoading(true);
      const data = await wishlistService.getMyWishlist();
      setWishlist(data);
    } catch (err) {
      setAlertType('error');
      setError(err.response?.data?.message || 'Erro ao carregar lista de desejos.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchWishlist();
  }, [fetchWishlist]);

  /**
   * Reseta o estado do formulário de adição/edição.
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
   * Inicia o fluxo de adição de um novo item.
   */
  const handleStartAdd = () => {
    if (wishlistItems.length >= MAX_WISHLIST_ITEMS) {
      setAlertType('warning');
      setError('Sua lista de desejos já possui o limite de 10 itens.');
      return;
    }
    resetForm();
    setIsAdding(true);
  };

  /**
   * Inicia o fluxo de edição de um item existente.
   * 
   * @param {Object} item Dados do item a ser editado.
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
   * Trata o salvamento (criação ou atualização) de um item.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validação básica de campos
    if (!nomeProduto.trim() || !link.trim()) {
      setAlertType('warning');
      setError('Preencha o nome do produto e o link.');
      return;
    }

    // Verifica limite antes de salvar um NOVO item
    if (!editingItem && wishlistItems.length >= MAX_WISHLIST_ITEMS) {
      setAlertType('warning');
      setError('Sua lista de desejos já possui o limite de 10 itens.');
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
        await wishlistService.updateItem(editingItem.id, payload);
      } else {
        await wishlistService.addItem(payload);
      }

      resetForm();
      await fetchWishlist(); // Recarrega a lista para refletir as mudanças
    } catch (err) {
      setAlertType('error');
      setError(err.response?.data?.message || 'Erro ao salvar item.');
    } finally {
      setActionLoading(false);
    }
  };

  /**
   * Abre o modal de confirmação para exclusão de um item.
   */
  const handleDelete = (itemId) => {
    setItemToDelete(itemId);
  };

  /**
   * Executa a exclusão definitiva do item após confirmação no modal.
   */
  const confirmDelete = async () => {
    if (!itemToDelete) return;
    try {
      await wishlistService.removeItem(itemToDelete);
      setItemToDelete(null);
      await fetchWishlist();
    } catch (err) {
      setAlertType('error');
      setError(err.response?.data?.message || 'Erro ao remover item.');
    }
  };

  const wishlistItems = wishlist?.itens || [];
  const hasReachedWishlistLimit = wishlistItems.length >= MAX_WISHLIST_ITEMS;

  return (
    <div className="wishlist-page">
      {/* Background Decorativo */}
      <div className="wishlist-background-decor" aria-hidden="true">
        <span className="gift gift-one">🎁</span>
        <span className="gift gift-two">🎁</span>
        <span className="dashed-square square-one"></span>
      </div>

      <main className="wishlist-main">
        <nav className="wishlist-breadcrumb">
          <button onClick={() => navigate('/dashboard')}>
            <ArrowLeft size={18} /> Voltar para o Dashboard
          </button>
        </nav>

        <header className="wishlist-header">
          <div>
            <span className="wishlist-kicker">Lista pessoal</span>
            <h1>Lista de Desejos</h1>
            <p>Gerencie os presentes que ficarão visíveis para seu amigo secreto após o sorteio.</p>
          </div>
        </header>

        {error && <p className={`form-alert ${alertType}`}>{error}</p>}

        <div className="wishlist-layout">
          {/* Painel de Visualização da Lista */}
          <section className="wishlist-list-panel">
            <div className="wishlist-section-header">
              <h2>Seus presentes</h2>
              <span>{wishlistItems.length}/{MAX_WISHLIST_ITEMS} itens</span>
            </div>

            {loading ? (
              <div className="wishlist-loading">
                <div className="spinner"></div>
                <p>Carregando desejos...</p>
              </div>
            ) : wishlistItems.length === 0 ? (
              <div className="wishlist-empty">
                <div className="wishlist-empty-icon">🎁</div>
                <h3>Sua lista está vazia</h3>
                <p>Adicione alguns presentes para ajudar quem tirou você!</p>
                <button className="btn-primary" onClick={handleStartAdd}>
                  <Plus size={18} /> Adicionar primeiro presente
                </button>
              </div>
            ) : (
              <div className="wishlist-items">
                {wishlistItems.map(item => (
                  <article key={item.id} className="wishlist-item-card">
                    <div className="wishlist-item-info">
                      <h3>{item.nomeProduto}</h3>
                      {item.link && (
                        <a href={item.link} target="_blank" rel="noopener noreferrer">
                          <ExternalLink size={15} /> Abrir link
                        </a>
                      )}
                    </div>
                    <div className="wishlist-item-actions">
                      <button onClick={() => handleStartEdit(item)} title="Editar item">
                        <Pencil size={17} />
                      </button>
                      <button className="delete" onClick={() => handleDelete(item.id)} title="Remover item">
                        <Trash2 size={17} />
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </section>

          {/* Painel Lateral de Formulário */}
          <aside className="wishlist-form-panel">
            <h2>{editingItem ? 'Editar presente' : 'Novo presente'}</h2>
            <p>
              {hasReachedWishlistLimit && !editingItem
                ? 'Sua lista atingiu o limite. Remova um item para adicionar um novo.'
                : 'Informe um nome claro e um link direto para o produto.'}
            </p>

            {isAdding ? (
              <form onSubmit={handleSubmit} className="wishlist-form" noValidate>
                <div className="input-group">
                  <label>Nome do Produto</label>
                  <input
                    type="text"
                    placeholder="Ex: Fone Bluetooth"
                    value={nomeProduto}
                    onChange={(e) => setNomeProduto(e.target.value)}
                    required
                    autoFocus
                  />
                </div>
                <div className="input-group">
                  <label>Link do Produto</label>
                  <input
                    type="url"
                    placeholder="https://..."
                    value={link}
                    onChange={(e) => setLink(e.target.value)}
                    required
                  />
                </div>
                <div className="wishlist-form-actions">
                  <button type="button" className="btn-secondary" onClick={resetForm}>Cancelar</button>
                  <button type="submit" className="btn-primary" disabled={actionLoading}>
                    {actionLoading ? 'Salvando...' : 'Salvar'}
                  </button>
                </div>
              </form>
            ) : (
              <button className="btn-primary wishlist-start-button" onClick={handleStartAdd} disabled={hasReachedWishlistLimit}>
                <Plus size={18} /> Adicionar presente
              </button>
            )}
          </aside>
        </div>
      </main>

      {/* Modal de Confirmação de Exclusão */}
      <ConfirmationModal
        isOpen={Boolean(itemToDelete)}
        onClose={() => setItemToDelete(null)}
        onConfirm={confirmDelete}
        title="Remover item?"
        message="Tem certeza que deseja remover este item da sua lista?"
        confirmText="Remover"
        variant="danger"
      />
    </div>
  );
};

export default Wishlist;
