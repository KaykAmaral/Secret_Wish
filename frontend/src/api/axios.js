import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// Interceptor para lidar com erros globais, especialmente 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Se receber 401 em uma rota protegida, podemos limpar o estado local se necessário
      // ou redirecionar para o login. 
      // Nota: O redirecionamento aqui pode ser agressivo, então geralmente tratamos no Context.
    }
    return Promise.reject(error);
  }
);

export default api;
