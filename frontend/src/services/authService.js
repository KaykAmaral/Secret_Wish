import api from '../api/axios';

const authService = {
  /**
   * Consulta o status atual da sessão.
   * Retorna { authenticated: boolean, user: { id, nome, email } | null }
   */
  getStatus: async () => {
    const response = await api.get('/api/auth/status');
    return response.data;
  },

  /**
   * Login com e-mail e senha.
   */
  login: async (email, password) => {
    const response = await api.post('/api/auth/login', { email, password });
    return response.data;
  },

  /**
   * Cadastro com e-mail e senha.
   */
  register: async (nome, email, password) => {
    const response = await api.post('/api/auth/register', { nome, email, password });
    return response.data;
  },

  /**
   * Inicia o fluxo de logout no backend.
   */
  logout: async () => {
    await api.post('/api/logout');
  },

  /**
   * Retorna a URL para o redirecionamento do OAuth2 Google.
   */
  getGoogleLoginUrl: () => {
    return 'http://localhost:8080/oauth2/authorization/google';
  }
};

export default authService;
