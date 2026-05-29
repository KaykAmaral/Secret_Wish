import api from '../api/axios';

/**
 * Serviço de notificações do sistema.
 * 
 * Centraliza o gerenciamento de alertas e avisos gerados pelo backend
 * (ex: sorteio realizado, novos membros, lembretes de eventos).
 */
const notificationService = {
  /**
   * Recupera todas as notificações destinadas ao usuário autenticado.
   * 
   * @returns {Promise<Array>} Lista de notificações ordenada por data de criação.
   */
  getNotifications: async () => {
    const response = await api.get('/api/notifications');
    return response.data;
  },

  /**
   * Atualiza o estado de uma notificação para 'lida'.
   * 
   * @param {number|string} notificationId ID da notificação.
   * @returns {Promise<Object>} A notificação atualizada.
   */
  markAsRead: async (notificationId) => {
    const response = await api.patch(`/api/notifications/${notificationId}/read`);
    return response.data;
  },

  /**
   * Remove permanentemente uma notificação do registro do usuário.
   * 
   * @param {number|string} notificationId ID da notificação.
   * @returns {Promise<void>}
   */
  deleteNotification: async (notificationId) => {
    await api.delete(`/api/notifications/${notificationId}`);
  },

  /**
   * Remove todas as notificações do histórico do usuário.
   * 
   * @returns {Promise<void>}
   */
  deleteAllNotifications: async () => {
    await api.delete('/api/notifications');
  }
};

export default notificationService;
