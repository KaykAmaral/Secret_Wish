import api from '../api/axios';

const wishlistService = {
  /**
   * Consulta a wishlist do usuário autenticado.
   */
  getMyWishlist: async () => {
    const response = await api.get('/api/wishlist');
    return response.data;
  },

  /**
   * Adiciona um item à wishlist.
   * @param {Object} item { nomeProduto, link }
   */
  addItem: async (item) => {
    const response = await api.post('/api/wishlist/items', item);
    return response.data;
  },

  /**
   * Atualiza um item da wishlist.
   * @param {number} itemId 
   * @param {Object} item { nomeProduto, link }
   */
  updateItem: async (itemId, item) => {
    const response = await api.put(`/api/wishlist/items/${itemId}`, item);
    return response.data;
  },

  /**
   * Remove um item da wishlist.
   */
  removeItem: async (itemId) => {
    await api.delete(`/api/wishlist/items/${itemId}`);
  },

  /**
   * Consulta a wishlist visível de outro usuário em um grupo.
   */
  getVisibleWishlist: async (groupId, ownerId) => {
    const response = await api.get(`/api/groups/${groupId}/users/${ownerId}/wishlist`);
    return response.data;
  },

  /**
   * Gera sugestão de IA para uma wishlist visível.
   */
  generateAiSuggestion: async (groupId, ownerId) => {
    const response = await api.post(`/api/groups/${groupId}/users/${ownerId}/wishlist/ai-suggestion`);
    return response.data;
  }
};

export default wishlistService;
