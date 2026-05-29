import api from '../api/axios';

/**
 * Serviço de autenticação responsável por gerenciar a sessão do usuário.
 * 
 * Este serviço atua como uma fachada para os endpoints de autenticação do backend,
 * abstraindo detalhes como a gestão de cookies HTTP-only e URLs de OAuth2.
 */
const authService = {
  /**
   * Consulta o status atual da sessão do usuário.
   * O backend utiliza o cookie 'secret_wish_token' (HTTP-only) enviado automaticamente pelo navegador.
   * 
   * @returns {Promise<{authenticated: boolean, user: Object|null}>} Objeto contendo o estado da autenticação e os dados do usuário.
   */
  getStatus: async () => {
    const response = await api.get('/api/auth/status');
    return response.data;
  },

  /**
   * Realiza a autenticação local utilizando e-mail e senha.
   * Em caso de sucesso, o backend define o cookie de sessão.
   * 
   * @param {string} email E-mail do usuário.
   * @param {string} password Senha em texto simples.
   * @returns {Promise<Object>} Dados do usuário autenticado e status da sessão.
   */
  login: async (email, password) => {
    const response = await api.post('/api/auth/login', { email, password });
    return response.data;
  },

  /**
   * Registra um novo usuário no sistema com e-mail e senha.
   * Após o registro, o usuário é automaticamente autenticado pelo backend.
   * 
   * @param {string} nome Nome completo do usuário.
   * @param {string} email E-mail único para o cadastro.
   * @param {string} password Senha de acesso.
   * @returns {Promise<Object>} Dados do novo usuário e status da sessão.
   */
  register: async (nome, email, password) => {
    const response = await api.post('/api/auth/register', { nome, email, password });
    return response.data;
  },

  /**
   * Encerra a sessão ativa do usuário tanto no cliente quanto no servidor.
   * O backend invalida o cookie de autenticação ao processar esta requisição.
   * 
   * @returns {Promise<void>}
   */
  logout: async () => {
    await api.post('/api/logout');
  },

  /**
   * Fornece a URL absoluta para iniciar o fluxo de login via Google OAuth2.
   * O redirecionamento para esta URL transfere o controle para o backend e, consequentemente, para o Google.
   * 
   * @returns {string} Endpoint de autorização do Spring Security.
   */
  getGoogleLoginUrl: () => {
    // Nota: Em produção, esta URL deve ser configurada dinamicamente ou ser relativa.
    return 'http://localhost:8080/oauth2/authorization/google';
  }
};

export default authService;
