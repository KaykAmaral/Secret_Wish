import api from '../api/axios';

/**
 * Serviço de chat e mensageria.
 * 
 * Gerencia a troca de mensagens anônimas e privadas entre os pares do sorteio.
 * Embora as notificações ocorram via WebSocket, este serviço provê a persistência e
 * recuperação oficial das conversas.
 */
const messageService = {
  /**
   * Envia uma mensagem para outro participante.
   * O backend valida se os usuários formam um par válido no sorteio para permitir a troca.
   * 
   * @param {number|string} groupId ID do grupo onde a conversa ocorre.
   * @param {number|string} destinatarioId ID do usuário que receberá a mensagem.
   * @param {string} conteudo Texto da mensagem.
   * @returns {Promise<Object>} A mensagem persistida com metadados de envio.
   */
  sendMessage: async (groupId, destinatarioId, conteudo) => {
    const response = await api.post(`/api/groups/${groupId}/messages`, {
      destinatarioId,
      conteudo
    });
    return response.data;
  },

  /**
   * Recupera o histórico de mensagens entre o usuário logado e outro participante.
   * 
   * @param {number|string} groupId ID do grupo.
   * @param {number|string} otherUserId ID do interlocutor.
   * @returns {Promise<Array>} Lista de mensagens ordenadas cronologicamente.
   */
  getConversation: async (groupId, otherUserId) => {
    const response = await api.get(`/api/groups/${groupId}/messages/${otherUserId}`);
    return response.data;
  },

  /**
   * Obtém a lista de conversas ativas do usuário em um grupo, incluindo contadores de não lidas.
   * Útil para renderizar a lista de chats ou gavetas de mensagens.
   * 
   * @param {number|string} groupId ID do grupo.
   * @returns {Promise<Array>} Resumo das conversas (nome exibição, anonimato, unreadCount).
   */
  getChatSummaries: async (groupId) => {
    const response = await api.get(`/api/groups/${groupId}/messages/chats`);
    return response.data;
  },

  /**
   * Notifica o servidor que o usuário visualizou as mensagens de uma conversa específica.
   * 
   * @param {number|string} groupId ID do grupo.
   * @param {number|string} otherUserId ID do interlocutor.
   * @returns {Promise<void>}
   */
  markAsRead: async (groupId, otherUserId) => {
    await api.patch(`/api/groups/${groupId}/messages/${otherUserId}/read`);
  },

  /**
   * Obtém o total de mensagens não lidas de todas as conversas do usuário.
   * Frequentemente usado para exibir badges globais de notificação.
   * 
   * @returns {Promise<{unreadCount: number}>} Objeto com o total de mensagens pendentes.
   */
  getUnreadCount: async () => {
    const response = await api.get('/api/messages/unread-count');
    return response.data;
  }
};

export default messageService;
