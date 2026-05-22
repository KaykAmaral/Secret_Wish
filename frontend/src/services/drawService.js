import api from '../api/axios';

const drawService = {
  /**
   * Realiza o sorteio de um grupo.
   * Apenas o dono do grupo pode realizar esta ação.
   */
  performDraw: async (groupId) => {
    const response = await api.post(`/api/groups/${groupId}/draw`);
    return response.data;
  },

  /**
   * Consulta quem o usuário autenticado tirou no sorteio de um grupo.
   */
  getWhoITook: async (groupId) => {
    const response = await api.get(`/api/groups/${groupId}/draw/me`);
    return response.data;
  }
};

export default drawService;
