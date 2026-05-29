import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * Serviço de Mensageria em Tempo Real (WebSocket).
 * 
 * Implementado como um Singleton, este serviço gerencia a conexão STOMP sobre SockJS.
 * Ele centraliza as assinaturas de tópicos e garante a reconexão automática,
 * evitando que múltiplos componentes abram conexões redundantes com o backend.
 */
class WebSocketService {
  constructor() {
    /** @type {Client|null} Instância do cliente STOMP */
    this.client = null;
    /** @type {Map<string, Function>} Mapa de tópicos ativos e seus respectivos callbacks */
    this.subscriptions = new Map();
    /** @type {Array<Function>} Callbacks executados quando a conexão é estabelecida */
    this.onConnectCallbacks = [];
  }

  /**
   * Inicializa a conexão com o servidor WebSocket.
   * Se já houver uma conexão ativa ou pendente, a chamada é ignorada.
   * 
   * @param {string|null} token JWT opcional para autenticação no handshake.
   */
  connect(token = null) {
    if (this.client && this.client.connected) return;

    // Nota: A URL deve ser ajustada para variáveis de ambiente em produção.
    const socket = new SockJS('http://localhost:8080/ws-sockjs');
    
    this.client = new Client({
      webSocketFactory: () => socket,
      // Passagem do token via header CONNECT para autenticação no Spring Security/STOMP.
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      debug: (str) => {
        // console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = (frame) => {
      console.log('STOMP Connected: ' + frame);
      this.onConnectCallbacks.forEach(callback => callback());
      
      // Ao reconectar, reinscreve automaticamente em todos os tópicos que estavam ativos.
      // Isso previne a perda de eventos em tempo real após instabilidades de rede.
      this.subscriptions.forEach((callback, topic) => {
        this._doSubscribe(topic, callback);
      });
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP Error', frame);
    };

    this.client.activate();
  }

  /**
   * Desativa a conexão e limpa os recursos.
   * Recomendado chamar ao fazer logout ou destruir componentes globais.
   */
  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
  }

  /**
   * Inscreve-se em um tópico específico para receber mensagens.
   * Se a conexão ainda não estiver pronta, a inscrição será realizada assim que o status mudar para 'Connected'.
   * 
   * @param {string} topic O caminho do tópico (ex: '/user/queue/messages').
   * @param {Function} callback Função que processará o payload recebido.
   * @returns {Object|undefined} Retorna o objeto de inscrição do STOMP se ativo.
   */
  subscribe(topic, callback) {
    this.subscriptions.set(topic, callback);
    if (this.client && this.client.connected) {
      return this._doSubscribe(topic, callback);
    }
  }

  /**
   * Executa a inscrição técnica via STOMP e realiza o parse automático do JSON.
   * 
   * @private
   */
  _doSubscribe(topic, callback) {
    return this.client.subscribe(topic, (message) => {
      callback(JSON.parse(message.body));
    });
  }

  /**
   * Registra um callback para ser executado imediatamente após uma conexão bem-sucedida.
   * Se já estiver conectado, executa o callback instantaneamente.
   * 
   * @param {Function} callback 
   */
  onConnect(callback) {
    this.onConnectCallbacks.push(callback);
    if (this.client && this.client.connected) {
      callback();
    }
  }
}

// Exporta uma instância única (Singleton) para toda a aplicação.
const webSocketService = new WebSocketService();
export default webSocketService;
