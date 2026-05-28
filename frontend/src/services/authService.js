import api from '../api/axios';

const authService = {
  /**
   * Consulta a sessao atual usando o cookie HTTP-only enviado pelo browser.
   */
  getStatus: async () => {
    const response = await api.get('/api/auth/status');
    return response.data;
  },

  /**
   * Realiza login local e deixa o backend definir o cookie de autenticacao.
   */
  login: async (email, password) => {
    const response = await api.post('/api/auth/login', { email, password });
    return response.data;
  },

  /**
   * Cria conta por email/senha e retorna a sessao ja autenticada.
   */
  register: async (nome, email, password) => {
    const response = await api.post('/api/auth/register', { nome, email, password });
    return response.data;
  },

  /**
   * Encerra a sessao no backend, removendo o cookie HTTP-only.
   */
  logout: async () => {
    await api.post('/api/logout');
  },

  /**
   * Retorna a URL externa que inicia o fluxo OAuth2 Google no backend.
   */
  getGoogleLoginUrl: () => {
    return 'http://localhost:8080/oauth2/authorization/google';
  }
};

export default authService;
