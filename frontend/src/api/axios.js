import axios from 'axios';

/**
 * Cliente HTTP base configurado com Axios.
 * 
 * Esta instância centraliza a comunicação com a API REST do backend,
 * garantindo que todas as requisições sigam os padrões de segurança e cabeçalhos necessários.
 */
const api = axios.create({
  // URL base do backend (Spring Boot). Deve ser movida para variáveis de ambiente (.env) em produção.
  baseURL: 'http://localhost:8080',
  
  // Habilita o envio de cookies (como o secret_wish_token) em requisições cross-origin.
  // Essencial para o funcionamento da autenticação via cookies HTTP-only.
  withCredentials: true,
  
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    // Previne o cache indesejado de respostas dinâmicas em alguns navegadores.
    'Cache-Control': 'no-cache',
    'Pragma': 'no-cache',
    'Expires': '0',
  },
});

/**
 * Interceptores de Resposta.
 * 
 * Permitem capturar erros globais (como 401 Unauthorized) antes que cheguem aos componentes,
 * facilitando o gerenciamento centralizado da validade da sessão.
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Caso receba 401, significa que o cookie expirou ou é inválido.
    if (error.response && error.response.status === 401) {
      // O tratamento agressivo (redirecionamento) é evitado aqui para permitir que o 
      // AuthProvider gerencie o estado de loading e feedback de forma mais suave.
      console.warn('[API] Sessão expirada ou acesso não autorizado.');
    }
    return Promise.reject(error);
  }
);

export default api;
