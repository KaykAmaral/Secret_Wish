import { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Send, Gift, ExternalLink, Sparkles } from 'lucide-react';
import messageService from '../../services/messageService';
import wishlistService from '../../services/wishlistService';
import webSocketService from '../../services/websocketService';
import './PremiumChatDrawer.css';

const PremiumChatDrawer = ({ isOpen, onClose, groupId, otherUserId, title, isAnonymousChat }) => {
  const [messages, setMessages] = useState([]);
  const [wishlist, setWishlist] = useState(null);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      fetchData();
      subscribeToMessages();
    }
    return () => {
      // Unsubscribe logic could be added to webSocketService if needed
    };
  }, [isOpen, otherUserId]);

  useEffect(scrollToBottom, [messages]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const msgs = await messageService.getConversation(groupId, otherUserId);
      setMessages(msgs);

      // Se for o chat com quem eu tirei, mostrar a wishlist dela
      if (!isAnonymousChat) {
        const wl = await wishlistService.getVisibleWishlist(groupId, otherUserId);
        setWishlist(wl);
      }
    } catch (err) {
      console.error('Erro ao carregar dados do chat:', err);
    } finally {
      setLoading(false);
    }
  };

  const subscribeToMessages = () => {
    webSocketService.subscribe('/user/queue/messages', (notification) => {
      if (notification.groupId === parseInt(groupId)) {
        fetchData(); 
      }
    });

    webSocketService.subscribe('/user/queue/wishlist-update', (notification) => {
      if (notification.groupId === parseInt(groupId)) {
        // Recarrega a wishlist se for o chat com quem eu tirei
        if (!isAnonymousChat) {
          fetchData();
        }
      }
    });
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    try {
      const sentMsg = await messageService.sendMessage(groupId, otherUserId, newMessage);
      setMessages(prev => [...prev, sentMsg]);
      setNewMessage('');
    } catch (err) {
      console.error('Erro ao enviar mensagem:', err);
    }
  };

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="drawer-overlay" onClick={onClose}>
        <motion.div 
          className="drawer-content"
          initial={{ x: '100%' }}
          animate={{ x: 0 }}
          exit={{ x: '100%' }}
          transition={{ type: 'spring', damping: 25, stiffness: 200 }}
          onClick={e => e.stopPropagation()}
        >
          <header className="drawer-header">
            <div className="header-info">
              <div className="status-indicator online" />
              <h2>{title}</h2>
            </div>
            <button className="close-btn" onClick={onClose}><X /></button>
          </header>

          <div className="chat-container">
            <div className="messages-list">
              {/* Mensagem Especial do Sistema com Wishlist */}
              {!isAnonymousChat && wishlist && wishlist.itens.length > 0 && (
                <div className="wishlist-system-card">
                  <div className="system-card-header">
                    <Gift size={18} className="text-purple" />
                    <span>Lista de desejos de <strong>{title}</strong></span>
                  </div>
                  <div className="wishlist-items-grid">
                    {wishlist.itens.map(item => (
                      <div key={item.id} className="wishlist-item-mini glass">
                        <span className="item-name">{item.nomeProduto}</span>
                        {item.link && (
                          <a href={item.link} target="_blank" rel="noopener noreferrer" className="item-link">
                            <ExternalLink size={14} />
                          </a>
                        )}
                      </div>
                    ))}
                  </div>
                  <div className="system-card-footer">
                    <Sparkles size={14} />
                    <span>IA analisou e recomenda foco nestes itens!</span>
                  </div>
                </div>
              )}

              {loading ? (
                <div className="loading-chat">
                  {[1, 2, 3].map(i => <div key={i} className="skeleton-bubble" />)}
                </div>
              ) : (
                messages.map((msg, idx) => (
                  <div key={msg.id || idx} className={`message-bubble ${msg.nomeRemetenteExibicao === 'amigo secreto' || msg.nomeRemetenteExibicao === 'Pessoa Anônima' ? 'other' : 'me'}`}>
                    {msg.conteudo}
                    <span className="message-time">
                      {new Date(msg.dataEnvio).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                ))
              )}
              <div ref={messagesEndRef} />
            </div>

            <form className="chat-input-area" onSubmit={handleSendMessage}>
              <input 
                type="text" 
                placeholder="Envie uma mensagem anônima..." 
                value={newMessage}
                onChange={e => setNewMessage(e.target.value)}
                className="chat-input"
              />
              <button type="submit" className="send-btn">
                <Send size={20} />
              </button>
            </form>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};

export default PremiumChatDrawer;
