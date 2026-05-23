import api from '../api/axios';

const groupService = {
  /**
   * Lista todos os grupos que o usuário participa.
   */
  getMyGroups: async () => {
    const response = await api.get('/api/groups');
    return response.data;
  },

  /**
   * Busca detalhes de um grupo específico.
   */
  getGroupById: async (groupId) => {
    const response = await api.get(`/api/groups/${groupId}`);
    return response.data;
  },

  /**
   * Cria um novo grupo.
   * @param {Object} data { nome, dataEvento }
   */
  createGroup: async (data) => {
    const response = await api.post('/api/groups', data);
    return response.data;
  },

  /**
   * Entra em um grupo usando o código único (XXXX-XXXX).
   * @param {string} codigoUnico 
   */
  joinGroup: async (codigoUnico) => {
    const response = await api.post('/api/groups/join', { codigoUnico });
    return response.data;
  },

  /**
   * Sai de um grupo.
   */
  leaveGroup: async (groupId) => {
    await api.delete(`/api/groups/${groupId}/leave`);
  },

  /**
   * Exclui um grupo (apenas dono).
   */
  deleteGroup: async (groupId) => {
    await api.delete(`/api/groups/${groupId}`);
  }
};

export default groupService;
