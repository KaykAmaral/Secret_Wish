import { useCallback, useEffect, useState } from 'react';
import { ArrowLeft, ExternalLink, Pencil, Plus, Trash2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import wishlistService from '../../services/wishlistService';
import './Wishlist.css';

const MAX_WISHLIST_ITEMS = 10;

const Wishlist = () => {
  const navigate = useNavigate();
  const [wishlist, setWishlist] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAdding, setIsAdding] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [nomeProduto, setNomeProduto] = useState('');
  const [link, setLink] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [alertType, setAlertType] = useState('error');

  // Mantem a pagina sincronizada com a lista persistida no backend.
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

  // Limpa o formulario e devolve o foco visual para a lista.
  const resetForm = () => {
    setNomeProduto('');
    setLink('');
    setEditingItem(null);
    setIsAdding(false);
    setError('');
    setAlertType('error');
  };

  const handleStartAdd = () => {
    if (wishlistItems.length >= MAX_WISHLIST_ITEMS) {
      setAlertType('warning');
      setError('Sua lista de desejos ja possui o limite de 10 itens.');
      return;
    }

    resetForm();
    setIsAdding(true);
  };

  // Reaproveita o mesmo formulario para criacao e edicao.
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
    if (!nomeProduto.trim() || !link.trim()) {
      setAlertType('warning');
      setError('Preencha nome do produto e link.');
      return;
    }

    if (!editingItem && wishlistItems.length >= MAX_WISHLIST_ITEMS) {
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
        await wishlistService.updateItem(editingItem.id, payload);
      } else {
        await wishlistService.addItem(payload);
      }

      resetForm();
      await fetchWishlist();
    } catch (err) {
      setAlertType('error');
      setError(err.response?.data?.message || 'Erro ao salvar item.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (itemId) => {
    if (!window.confirm('Tem certeza que deseja remover este item?')) return;

    try {
      await wishlistService.removeItem(itemId);
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
      <div className="wishlist-background-decor" aria-hidden="true">
        <span className="gift gift-one">🎁</span>
        <span className="gift gift-two">🎁</span>
        <span className="gift gift-three">🎁</span>
        <span className="gift gift-four">🎁</span>
        <span className="dashed-square square-one"></span>
        <span className="dashed-square square-two"></span>
        <span className="dashed-square square-three"></span>
        <span className="dashed-square square-four"></span>
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
            <p>Gerencie os presentes que ficarao visiveis para seu amigo secreto depois do sorteio.</p>
          </div>
        </header>

        {error && <p className={`form-alert ${alertType}`}>{error}</p>}

        <div className="wishlist-layout">
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
                <h3>Hmm... parece que sua lista está vazia</h3>
                <p>Adicione alguns presentes para dar uma ajuda para quem tirou voce no amigo secreto.</p>
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
                      <button onClick={() => handleStartEdit(item)} title="Editar item" aria-label="Editar item">
                        <Pencil size={17} />
                      </button>
                      <button className="delete" onClick={() => handleDelete(item.id)} title="Remover item" aria-label="Remover item">
                        <Trash2 size={17} />
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </section>

          <aside className="wishlist-form-panel">
            <h2>{editingItem ? 'Editar presente' : 'Novo presente'}</h2>
            <p>
              {hasReachedWishlistLimit && !editingItem
                ? 'Sua lista ja chegou ao limite de 10 itens. Remova um presente para adicionar outro.'
                : 'Informe um nome claro e um link direto para o produto.'}
            </p>

            {isAdding ? (
              <form onSubmit={handleSubmit} className="wishlist-form" noValidate>
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
    </div>
  );
};

export default Wishlist;
