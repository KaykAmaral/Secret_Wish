import api from '../api/axios';

const groupService = {
  /**
   * Lista todos os grupos em que o usuario autenticado participa.
   */
  getMyGroups: async () => {
    const response = await api.get('/api/groups');
    return response.data;
  },

  /**
   * Busca detalhes de um grupo apenas se o backend reconhecer o usuario como participante.
   */
  getGroupById: async (groupId) => {
    const response = await api.get(`/api/groups/${groupId}`);
    return response.data;
  },

  /**
   * Cria um novo grupo com o usuario autenticado como dono e primeiro membro.
   * @param {Object} data { nome, descricao, dataEvento }
   */
  createGroup: async (data) => {
    const response = await api.post('/api/groups', data);
    return response.data;
  },

  /**
   * Entra em um grupo usando o codigo publico no formato XXXX-XXXX.
   */
  joinGroup: async (codigoUnico) => {
    const response = await api.post('/api/groups/join', { codigoUnico });
    return response.data;
  },

  /**
   * Sai de um grupo antes do sorteio; o backend bloqueia a saida do dono.
   */
  leaveGroup: async (groupId) => {
    await api.delete(`/api/groups/${groupId}/leave`);
  },

  /**
   * Exclui um grupo e dados relacionados; permitido apenas para o dono.
   */
  deleteGroup: async (groupId) => {
    await api.delete(`/api/groups/${groupId}`);
  }
};

export default groupService;
