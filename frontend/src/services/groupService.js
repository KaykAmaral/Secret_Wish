import api from '../api/axios';

/**
 * Serviço de gerenciamento de grupos.
 * 
 * Lida com o ciclo de vida dos grupos, desde a criação e entrada de membros
 * até a exclusão final. Todas as regras de negócio (limite de grupos por dono,
 * formato do código único, etc.) são validadas pelo backend.
 */
const groupService = {
  /**
   * Recupera todos os grupos dos quais o usuário autenticado faz parte.
   * 
   * @returns {Promise<Array>} Lista de objetos de grupo.
   */
  getMyGroups: async () => {
    const response = await api.get('/api/groups');
    return response.data;
  },

  /**
   * Obtém os detalhes completos de um grupo específico.
   * O acesso é restrito a usuários que já são membros do grupo.
   * 
   * @param {number|string} groupId ID do grupo.
   * @returns {Promise<Object>} Dados detalhados do grupo (membros, dono, datas).
   */
  getGroupById: async (groupId) => {
    const response = await api.get(`/api/groups/${groupId}`);
    return response.data;
  },

  /**
   * Cria um novo grupo de Amigo Secreto.
   * O usuário que cria o grupo é automaticamente definido como dono e membro.
   * 
   * @param {Object} data Objeto contendo nome, descrição e data do evento.
   * @returns {Promise<Object>} O grupo recém-criado, incluindo o código único gerado.
   */
  createGroup: async (data) => {
    const response = await api.post('/api/groups', data);
    return response.data;
  },

  /**
   * Permite que um usuário entre em um grupo existente através de um código de convite.
   * O código deve seguir o padrão 'XXXX-XXXX'.
   * 
   * @param {string} codigoUnico Código de acesso do grupo.
   * @returns {Promise<Object>} Detalhes do grupo ao qual o usuário acabou de se juntar.
   */
  joinGroup: async (codigoUnico) => {
    const response = await api.post('/api/groups/join', { codigoUnico });
    return response.data;
  },

  /**
   * Remove o usuário autenticado de um grupo.
   * Esta operação só é permitida antes do sorteio ser realizado. Donos não podem sair de seus próprios grupos (devem excluí-los).
   * 
   * @param {number|string} groupId ID do grupo.
   * @returns {Promise<void>}
   */
  leaveGroup: async (groupId) => {
    await api.delete(`/api/groups/${groupId}/leave`);
  },

  /**
   * Exclui permanentemente um grupo e todos os dados associados (sorteios, mensagens).
   * Restrito ao dono do grupo.
   * 
   * @param {number|string} groupId ID do grupo.
   * @returns {Promise<void>}
   */
  deleteGroup: async (groupId) => {
    await api.delete(`/api/groups/${groupId}`);
  }
};

export default groupService;
