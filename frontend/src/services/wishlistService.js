import api from '../api/axios';

const wishlistService = {
  /**
   * Consulta a wishlist do usuario autenticado; o backend cria uma lista vazia quando necessario.
   */
  getMyWishlist: async () => {
    const response = await api.get('/api/wishlist');
    return response.data;
  },

  /**
   * Adiciona item visivel para quem tirar este usuario apos o sorteio.
   */
  addItem: async (item) => {
    const response = await api.post('/api/wishlist/items', item);
    return response.data;
  },

  /**
   * Atualiza item da propria wishlist mantendo validacao de propriedade no backend.
   */
  updateItem: async (itemId, item) => {
    const response = await api.put(`/api/wishlist/items/${itemId}`, item);
    return response.data;
  },

  /**
   * Remove item da propria wishlist.
   */
  removeItem: async (itemId) => {
    await api.delete(`/api/wishlist/items/${itemId}`);
  },

  /**
   * Consulta a wishlist de outro usuario quando a regra do sorteio permite visualizacao.
   */
  getVisibleWishlist: async (groupId, ownerId) => {
    const response = await api.get(`/api/groups/${groupId}/users/${ownerId}/wishlist`);
    return response.data;
  },

  /**
   * Solicita sugestao de IA baseada somente nos itens da wishlist visivel.
   */
  generateAiSuggestion: async (groupId, ownerId) => {
    const response = await api.post(`/api/groups/${groupId}/users/${ownerId}/wishlist/ai-suggestion`);
    return response.data;
  }
};

export default wishlistService;
