-- Otimiza leitura/marcacao de mensagens nao lidas por conversa.
CREATE INDEX idx_messages_conversation_read
    ON messages (group_id, receiver_id, sender_id, lida);

-- Otimiza a busca cronologica da conversa bilateral.
CREATE INDEX idx_messages_conversation_sent_at
    ON messages (group_id, sender_id, receiver_id, data_envio);

-- Otimiza contador e update em lote de notificacoes nao lidas.
CREATE INDEX idx_notifications_user_read
    ON notifications (user_id, lida);

-- Otimiza carregamento dos itens ao serializar uma wishlist.
CREATE INDEX idx_wishlist_items_wishlist
    ON wishlist_items (wishlist_id);
