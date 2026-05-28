import api from '../api/axios';

const notificationService = {
  /**
   * Lista notificacoes do usuario em ordem definida pelo backend para exibicao no dashboard.
   */
  getNotifications: async () => {
    const response = await api.get('/api/notifications');
    return response.data;
  },

  /**
   * Marca uma notificacao especifica como lida mantendo a checagem de propriedade no backend.
   */
  markAsRead: async (notificationId) => {
    const response = await api.patch(`/api/notifications/${notificationId}/read`);
    return response.data;
  },

  /**
   * Exclui uma notificacao individual do usuario autenticado.
   */
  deleteNotification: async (notificationId) => {
    await api.delete(`/api/notifications/${notificationId}`);
  },

  /**
   * Limpa todo o historico de notificacoes do usuario.
   */
  deleteAllNotifications: async () => {
    await api.delete('/api/notifications');
  }
};

export default notificationService;
