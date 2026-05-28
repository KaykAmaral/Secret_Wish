import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = new Map();
    this.onConnectCallbacks = [];
  }

  // Reutiliza a conexao ativa para evitar multiplas sessoes STOMP por usuario.
  connect(token = null) {
    if (this.client && this.client.connected) return;

    const socket = new SockJS('http://localhost:8080/ws-sockjs');
    
    this.client = new Client({
      webSocketFactory: () => socket,
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
      // Reinscreve topicos apos reconexao automatica para nao perder notificacoes em tempo real.
      this.subscriptions.forEach((callback, topic) => {
        this._doSubscribe(topic, callback);
      });
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP Error', frame);
    };

    this.client.activate();
  }

  // Fecha a conexao quando a tela protegida sai do ciclo de vida.
  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
  }

  // Guarda a inscricao desejada mesmo antes da conexao estar pronta.
  subscribe(topic, callback) {
    this.subscriptions.set(topic, callback);
    if (this.client && this.client.connected) {
      return this._doSubscribe(topic, callback);
    }
  }

  // Normaliza o payload STOMP para objetos JavaScript usados pelos componentes.
  _doSubscribe(topic, callback) {
    return this.client.subscribe(topic, (message) => {
      callback(JSON.parse(message.body));
    });
  }

  // Permite registrar efeitos que dependem de uma conexao STOMP ja aberta.
  onConnect(callback) {
    this.onConnectCallbacks.push(callback);
    if (this.client && this.client.connected) {
      callback();
    }
  }
}

const webSocketService = new WebSocketService();
export default webSocketService;
