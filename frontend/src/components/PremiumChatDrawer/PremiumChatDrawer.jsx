import { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Send, Gift, ExternalLink, Sparkles, Smile } from 'lucide-react';
import EmojiPicker, { Theme } from 'emoji-picker-react';
import messageService from '../../services/messageService';
import wishlistService from '../../services/wishlistService';
import webSocketService from '../../services/websocketService';
import './PremiumChatDrawer.css';

const PremiumChatDrawer = ({ isOpen, onClose, groupId, otherUserId, title, isAnonymousChat }) => {
  const [messages, setMessages] = useState([]);
  const [wishlist, setWishlist] = useState(null);
  const [wishlistLoaded, setWishlistLoaded] = useState(false);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const messagesEndRef = useRef(null);
  const emojiPickerRef = useRef(null);

  // Mantem a conversa posicionada na mensagem mais recente apos novas mensagens.
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    // Fecha o seletor de emojis se clicar fora dele
    const handleClickOutside = (event) => {
      if (emojiPickerRef.current && !emojiPickerRef.current.contains(event.target)) {
        setShowEmojiPicker(false);
      }
    };

    if (showEmojiPicker) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showEmojiPicker]);

  useEffect(() => {
    if (isOpen) {
      // Abrir o drawer sempre sincroniza conversa e inscricoes relevantes.
      fetchData();
      subscribeToMessages();
      setShowEmojiPicker(false);
    }
    return () => {
      // Unsubscribe logic could be added to webSocketService if needed
    };
  }, [isOpen, otherUserId]);

  // Mensagens novas, inclusive via WebSocket, devem puxar o scroll para o fim.
  useEffect(scrollToBottom, [messages]);

  // Carrega conversa e, quando o usuario e o presenteador, a wishlist da pessoa sorteada.
  const fetchData = async () => {
    setLoading(true);
    setWishlistLoaded(false);
    try {
      const msgs = await messageService.getConversation(groupId, otherUserId);
      setMessages(msgs);

      // Se for o chat com quem eu tirei, mostrar a wishlist dela
      if (!isAnonymousChat) {
        try {
          const wl = await wishlistService.getVisibleWishlist(groupId, otherUserId);
          setWishlist(wl);
        } catch (wishlistErr) {
          console.error('Erro ao carregar wishlist do chat:', wishlistErr);
          setWishlist({ itens: [] });
        } finally {
          setWishlistLoaded(true);
        }
      } else {
        setWishlist(null);
      }
    } catch (err) {
      console.error('Erro ao carregar dados do chat:', err);
    } finally {
      setLoading(false);
    }
  };

  // Assina eventos privados que podem alterar a conversa ou o card de wishlist.
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
    if (e) e.preventDefault();
    // Evita mensagens vazias e preserva o input se o envio falhar.
    if (!newMessage.trim()) return;

    try {
      const sentMsg = await messageService.sendMessage(groupId, otherUserId, newMessage);
      setMessages(prev => [...prev, sentMsg]);
      setNewMessage('');
      setShowEmojiPicker(false);
    } catch (err) {
      console.error('Erro ao enviar mensagem:', err);
    }
  };

  const onEmojiClick = (emojiData) => {
    setNewMessage(prev => prev + emojiData.emoji);
  };

  if (!isOpen) return null;

  const wishlistItems = wishlist?.itens || [];

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
              {!isAnonymousChat && wishlistLoaded && (
                <div className="wishlist-system-card">
                  <div className="system-card-header">
                    <Gift size={18} className="text-purple" />
                    <span>Lista de desejos de <strong>{title}</strong></span>
                  </div>
                  {wishlistItems.length > 0 ? (
                    <>
                      <div className="wishlist-items-grid">
                        {wishlistItems.map(item => (
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
                    </>
                  ) : (
                    <div className="wishlist-empty-notice">
                      <p><strong>{title}</strong> ainda nao adicionou itens a lista de desejos.</p>
                    </div>
                  )}
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

            <div className="chat-input-wrapper">
              <AnimatePresence>
                {showEmojiPicker && (
                  <motion.div 
                    className="emoji-picker-container"
                    ref={emojiPickerRef}
                    initial={{ opacity: 0, y: 10, scale: 0.95 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    exit={{ opacity: 0, y: 10, scale: 0.95 }}
                    transition={{ duration: 0.2 }}
                  >
                    <EmojiPicker 
                      onEmojiClick={onEmojiClick}
                      theme={Theme.DARK}
                      lazyLoadEmojis={true}
                      searchPlaceHolder="Buscar emoji..."
                      width="100%"
                      height="350px"
                      skinTonesDisabled={true}
                      previewConfig={{ showPreview: false }}
                    />
                  </motion.div>
                )}
              </AnimatePresence>

              <form className="chat-input-area" onSubmit={handleSendMessage}>
                <button 
                  type="button" 
                  className={`emoji-toggle-btn ${showEmojiPicker ? 'active' : ''}`}
                  onClick={() => setShowEmojiPicker(!showEmojiPicker)}
                >
                  <Smile size={22} />
                </button>
                <input 
                  type="text" 
                  placeholder="Envie uma mensagem anônima..." 
                  value={newMessage}
                  onChange={e => setNewMessage(e.target.value)}
                  className="chat-input"
                  onFocus={() => setShowEmojiPicker(false)}
                />
                <button type="submit" className="send-btn" disabled={!newMessage.trim()}>
                  <Send size={20} />
                </button>
              </form>
            </div>
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};

export default PremiumChatDrawer;
