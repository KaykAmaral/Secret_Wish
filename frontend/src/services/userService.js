import api from '../api/axios';

const userService = {
  /**
   * Consulta o perfil do usuario autenticado para sincronizar dados do header e modal.
   */
  getMe: async () => {
    const response = await api.get('/api/me');
    return response.data;
  },

  /**
   * Atualiza somente campos editaveis pelo usuario; email permanece controlado pelo backend.
   */
  updateProfile: async (data) => {
    const response = await api.put('/api/me', data);
    return response.data;
  },

  /**
   * Solicita exclusao permanente da conta autenticada.
   */
  deleteAccount: async () => {
    await api.delete('/api/me');
  }
};

export default userService;
