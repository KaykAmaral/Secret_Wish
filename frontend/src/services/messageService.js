import api from '../api/axios';

const messageService = {
  /**
   * Envia mensagem privada para uma das pontas permitidas pelo sorteio do grupo.
   */
  sendMessage: async (groupId, destinatarioId, conteudo) => {
    const response = await api.post(`/api/groups/${groupId}/messages`, {
      destinatarioId,
      conteudo
    });
    return response.data;
  },

  /**
   * Busca a conversa bilateral entre o usuario autenticado e outro participante autorizado.
   */
  getConversation: async (groupId, otherUserId) => {
    const response = await api.get(`/api/groups/${groupId}/messages/${otherUserId}`);
    return response.data;
  },

  /**
   * Lista os chats que o usuario pode abrir depois que o sorteio foi realizado.
   */
  getChatSummaries: async (groupId) => {
    const response = await api.get(`/api/groups/${groupId}/messages/chats`);
    return response.data;
  },

  /**
   * Marca como lidas as mensagens recebidas nessa conversa.
   */
  markAsRead: async (groupId, otherUserId) => {
    await api.patch(`/api/groups/${groupId}/messages/${otherUserId}/read`);
  },

  /**
   * Busca o total global usado em badges de mensagens nao lidas.
   */
  getUnreadCount: async () => {
    const response = await api.get('/api/messages/unread-count');
    return response.data;
  }
};

export default messageService;
