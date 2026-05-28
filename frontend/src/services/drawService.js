import api from '../api/axios';

const drawService = {
  /**
   * Realiza o sorteio do grupo; o backend valida se o usuario e dono e se ha participantes suficientes.
   */
  performDraw: async (groupId) => {
    const response = await api.post(`/api/groups/${groupId}/draw`);
    return response.data;
  },

  /**
   * Consulta apenas o resultado do usuario autenticado, sem expor o sorteio completo.
   */
  getWhoITook: async (groupId) => {
    const response = await api.get(`/api/groups/${groupId}/draw/me`);
    return response.data;
  }
};

export default drawService;
