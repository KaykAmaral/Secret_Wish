import api from '../api/axios';

/**
 * Serviço de gerenciamento de listas de desejos (Wishlists).
 * 
 * Permite que usuários cadastrem itens que gostariam de ganhar, facilitando a escolha
 * para seu Amigo Secreto. Inclui integração indireta com IA (via backend) para sugestões.
 */
const wishlistService = {
  /**
   * Obtém a wishlist completa do usuário autenticado.
   * Se o usuário ainda não possuir uma lista, o backend cria uma automaticamente.
   * 
   * @returns {Promise<Object>} Dados da wishlist e lista de itens.
   */
  getMyWishlist: async () => {
    const response = await api.get('/api/wishlist');
    return response.data;
  },

  /**
   * Adiciona um novo item à lista de desejos.
   * 
   * @param {Object} item Objeto contendo nomeProduto e opcionalmente um link.
   * @returns {Promise<Object>} O item recém-criado.
   */
  addItem: async (item) => {
    const response = await api.post('/api/wishlist/items', item);
    return response.data;
  },

  /**
   * Atualiza as informações de um item existente na wishlist.
   * 
   * @param {number|string} itemId ID do item.
   * @param {Object} item Objeto com os campos atualizados (nomeProduto, link).
   * @returns {Promise<Object>} O item atualizado.
   */
  updateItem: async (itemId, item) => {
    const response = await api.put(`/api/wishlist/items/${itemId}`, item);
    return response.data;
  },

  /**
   * Remove permanentemente um item da wishlist.
   * 
   * @param {number|string} itemId ID do item.
   * @returns {Promise<void>}
   */
  removeItem: async (itemId) => {
    await api.delete(`/api/wishlist/items/${itemId}`);
  },

  /**
   * Recupera a wishlist de outro participante, desde que permitido pela regra do sorteio.
   * Útil para o usuário visualizar os desejos da pessoa que ele tirou.
   * 
   * @param {number|string} groupId ID do grupo.
   * @param {number|string} ownerId ID do dono da wishlist (seu Amigo Secreto).
   * @returns {Promise<Object>} Dados da wishlist visível.
   */
  getVisibleWishlist: async (groupId, ownerId) => {
    const response = await api.get(`/api/groups/${groupId}/users/${ownerId}/wishlist`);
    return response.data;
  }
};

export default wishlistService;
