import api from '../api/axios';

const notificationService = {
  /**
   * Lista todas as notificações do usuário.
   */
  getNotifications: async () => {
    const response = await api.get('/api/notifications');
    return response.data;
  },

  /**
   * Marca uma notificação como lida.
   */
  markAsRead: async (notificationId) => {
    const response = await api.patch(`/api/notifications/${notificationId}/read`);
    return response.data;
  },

  /**
   * Exclui uma notificação.
   */
  deleteNotification: async (notificationId) => {
    await api.delete(`/api/notifications/${notificationId}`);
  },

  /**
   * Exclui todas as notificações do usuário.
   */
  deleteAllNotifications: async () => {
    await api.delete('/api/notifications');
  }
};

export default notificationService;
