import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = new Map();
    this.onConnectCallbacks = [];
  }

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
      // Re-subscribe to all active subscriptions
      this.subscriptions.forEach((callback, topic) => {
        this._doSubscribe(topic, callback);
      });
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP Error', frame);
    };

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
    }
  }

  subscribe(topic, callback) {
    this.subscriptions.set(topic, callback);
    if (this.client && this.client.connected) {
      return this._doSubscribe(topic, callback);
    }
  }

  _doSubscribe(topic, callback) {
    return this.client.subscribe(topic, (message) => {
      callback(JSON.parse(message.body));
    });
  }

  onConnect(callback) {
    this.onConnectCallbacks.push(callback);
    if (this.client && this.client.connected) {
      callback();
    }
  }
}

const webSocketService = new WebSocketService();
export default webSocketService;
