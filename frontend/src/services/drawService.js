import api from '../api/axios';

/**
 * Serviço responsável por gerenciar as operações de sorteio do Amigo Secreto.
 * 
 * Por razões de privacidade e integridade do jogo, o frontend nunca tem acesso
 * ao mapeamento completo do sorteio, apenas ao resultado individual do usuário logado.
 */
const drawService = {
  /**
   * Aciona o algoritmo de sorteio para um grupo específico.
   * Requisito: O usuário deve ser o dono do grupo e o grupo deve ter pelo menos 3 participantes.
   * 
   * @param {number|string} groupId Identificador único do grupo.
   * @returns {Promise<Object>} Confirmação do sorteio com data de execução e contagem de participantes.
   */
  performDraw: async (groupId) => {
    const response = await api.post(`/api/groups/${groupId}/draw`);
    return response.data;
  },

  /**
   * Recupera o resultado do sorteio para o usuário autenticado dentro de um grupo.
   * Retorna os dados do "Amigo Secreto" sorteado e sua respectiva wishlist.
   * 
   * @param {number|string} groupId Identificador único do grupo.
   * @returns {Promise<Object>} Dados do amigo sorteado e sua lista de desejos.
   */
  getWhoITook: async (groupId) => {
    const response = await api.get(`/api/groups/${groupId}/draw/me`);
    return response.data;
  }
};

export default drawService;
