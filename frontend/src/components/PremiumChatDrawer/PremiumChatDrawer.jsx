import { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Send, Gift, ExternalLink, Sparkles, Smile } from 'lucide-react';
import EmojiPicker, { Theme } from 'emoji-picker-react';
import messageService from '../../services/messageService';
import wishlistService from '../../services/wishlistService';
import webSocketService from '../../services/websocketService';
import './PremiumChatDrawer.css';

/**
 * Gaveta de Chat Premium (PremiumChatDrawer).
 * 
 * Este componente gerencia a interface de chat anônimo e privado. 
 * Ele é "Premium" por integrar a exibição dinâmica da wishlist do amigo secreto
 * e suporte a emojis, além de lidar com atualizações em tempo real via WebSockets.
 */
const PremiumChatDrawer = ({ isOpen, onClose, groupId, otherUserId, title, isAnonymousChat }) => {
  // Estados de Dados da Conversa
  const [messages, setMessages] = useState([]);
  const [wishlist, setWishlist] = useState(null);
  const [wishlistLoaded, setWishlistLoaded] = useState(false);
  
  // Estados de UI e Formulário
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  
  // Referências para manipulação direta de DOM (Scroll e Clique Externo)
  const messagesEndRef = useRef(null);
  const emojiPickerRef = useRef(null);

  /**
   * Mantém o chat sempre focado na mensagem mais recente (autoscroll).
   */
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  /**
   * Gerencia o fechamento do seletor de emojis ao clicar fora da sua área.
   */
  useEffect(() => {
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

  /**
   * Sincroniza dados e inscrições de rede sempre que o chat é aberto ou o interlocutor muda.
   */
  useEffect(() => {
    if (isOpen) {
      fetchData();
      subscribeToMessages();
      setShowEmojiPicker(false);
    }
  }, [isOpen, otherUserId]);

  // Dispara o scroll automático sempre que a lista de mensagens é atualizada.
  useEffect(scrollToBottom, [messages]);

  /**
   * Busca o histórico da conversa e a wishlist (se o usuário for o presenteador).
   */
  const fetchData = async () => {
    setLoading(true);
    setWishlistLoaded(false);
    try {
      const msgs = await messageService.getConversation(groupId, otherUserId);
      setMessages(msgs);

      // Se NÃO for anônimo para o usuário logado, significa que ele está vendo seu "Amigo Tirado"
      // e, portanto, tem permissão para ver a wishlist desta pessoa.
      if (!isAnonymousChat) {
        try {
          const wl = await wishlistService.getVisibleWishlist(groupId, otherUserId);
          setWishlist(wl);
        } catch (wishlistErr) {
          console.error('[Chat] Erro ao carregar wishlist:', wishlistErr);
          setWishlist({ itens: [] });
        } finally {
          setWishlistLoaded(true);
        }
      } else {
        setWishlist(null);
      }
    } catch (err) {
      console.error('[Chat] Erro ao carregar dados:', err);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Assina os tópicos do WebSocket para receber notificações em tempo real.
   */
  const subscribeToMessages = () => {
    // Escuta novas mensagens para atualizar o histórico instantaneamente.
    webSocketService.subscribe('/user/queue/messages', (notification) => {
      if (notification.groupId === parseInt(groupId)) {
        fetchData(); 
      }
    });

    // Escuta mudanças na wishlist para manter o card de presentes atualizado.
    webSocketService.subscribe('/user/queue/wishlist-update', (notification) => {
      if (notification.groupId === parseInt(groupId) && !isAnonymousChat) {
        fetchData();
      }
    });
  };

  /**
   * Trata o envio de uma nova mensagem.
   */
  const handleSendMessage = async (e) => {
    if (e) e.preventDefault();
    if (!newMessage.trim()) return;

    try {
      const sentMsg = await messageService.sendMessage(groupId, otherUserId, newMessage);
      setMessages(prev => [...prev, sentMsg]);
      setNewMessage('');
      setShowEmojiPicker(false);
    } catch (err) {
      console.error('[Chat] Erro ao enviar mensagem:', err);
    }
  };

  /**
   * Insere o emoji selecionado no campo de texto da mensagem.
   */
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
          {/* Cabeçalho do Chat */}
          <header className="drawer-header">
            <div className="header-info">
              <div className="status-indicator online" />
              <h2>{title}</h2>
            </div>
            <button className="close-btn" onClick={onClose} aria-label="Fechar"><X /></button>
          </header>

          <div className="chat-container">
            <div className="messages-list">
              {/* Exibição da Wishlist (Apenas se permitido e carregado) */}
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
                              <a href={item.link} target="_blank" rel="noopener noreferrer" className="item-link" title="Ver produto">
                                <ExternalLink size={14} />
                              </a>
                            )}
                          </div>
                        ))}
                      </div>
                      <div className="system-card-footer">
                        <Sparkles size={14} />
                        <span>Dica: Escolha algo que combine com o perfil!</span>
                      </div>
                    </>
                  ) : (
                    <div className="wishlist-empty-notice">
                      <p><strong>{title}</strong> ainda não adicionou itens à lista.</p>
                    </div>
                  )}
                </div>
              )}

              {/* Lista de Mensagens Dinâmica */}
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

            {/* Rodapé: Input de Mensagem e Emoji Picker */}
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
                  aria-label="Emojis"
                >
                  <Smile size={22} />
                </button>
                <input 
                  type="text" 
                  placeholder="Envie uma mensagem..." 
                  value={newMessage}
                  onChange={e => setNewMessage(e.target.value)}
                  className="chat-input"
                  onFocus={() => setShowEmojiPicker(false)}
                />
                <button type="submit" className="send-btn" disabled={!newMessage.trim()} aria-label="Enviar">
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
