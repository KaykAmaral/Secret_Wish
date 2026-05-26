import api from '../api/axios';

const messageService = {
  /**
   * Envia uma mensagem no grupo.
   */
  sendMessage: async (groupId, destinatarioId, conteudo) => {
    const response = await api.post(`/api/groups/${groupId}/messages`, {
      destinatarioId,
      conteudo
    });
    return response.data;
  },

  /**
   * Busca a conversa com outro usuário.
   */
  getConversation: async (groupId, otherUserId) => {
    const response = await api.get(`/api/groups/${groupId}/messages/${otherUserId}`);
    return response.data;
  },

  /**
   * Lista os resumos de chat no grupo.
   */
  getChatSummaries: async (groupId) => {
    const response = await api.get(`/api/groups/${groupId}/messages/chats`);
    return response.data;
  },

  /**
   * Marca uma conversa como lida.
   */
  markAsRead: async (groupId, otherUserId) => {
    await api.patch(`/api/groups/${groupId}/messages/${otherUserId}/read`);
  },

  /**
   * Busca o contador total de mensagens não lidas.
   */
  getUnreadCount: async () => {
    const response = await api.get('/api/messages/unread-count');
    return response.data;
  }
};

export default messageService;
