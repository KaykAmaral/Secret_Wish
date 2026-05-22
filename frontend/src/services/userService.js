import api from '../api/axios';

const userService = {
  /**
   * Consulta os dados do usuário autenticado.
   */
  getMe: async () => {
    const response = await api.get('/api/me');
    return response.data;
  },

  /**
   * Atualiza os dados do perfil (nome e imagem).
   * @param {Object} data { nome, imagemUrl }
   */
  updateProfile: async (data) => {
    const response = await api.put('/api/me', data);
    return response.data;
  },

  /**
   * Exclui a conta do usuário autenticado.
   */
  deleteAccount: async () => {
    await api.delete('/api/me');
  }
};

export default userService;
