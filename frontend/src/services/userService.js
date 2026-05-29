import api from '../api/axios';

/**
 * Serviço de gerenciamento de dados do usuário.
 * 
 * Permite a consulta e atualização do perfil do próprio usuário autenticado.
 * Mantido separado do AuthService para isolar preocupações de conta vs. sessão.
 */
const userService = {
  /**
   * Recupera os dados completos do perfil do usuário logado.
   * 
   * @returns {Promise<Object>} Objeto contendo nome, e-mail, imagemUrl, etc.
   */
  getMe: async () => {
    const response = await api.get('/api/me');
    return response.data;
  },

  /**
   * Atualiza as informações do perfil do usuário.
   * O backend restringe quais campos podem ser alterados por este endpoint (ex: nome, imagemUrl).
   * 
   * @param {Object} data Objeto com os campos a serem atualizados.
   * @returns {Promise<Object>} O perfil atualizado.
   */
  updateProfile: async (data) => {
    const response = await api.put('/api/me', data);
    return response.data;
  },

  /**
   * Solicita a exclusão definitiva da conta do usuário e todos os seus dados.
   * Atenção: Esta operação é irreversível.
   * 
   * @returns {Promise<void>}
   */
  deleteAccount: async () => {
    await api.delete('/api/me');
  }
};

export default userService;
