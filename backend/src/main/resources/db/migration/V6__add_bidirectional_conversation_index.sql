-- Otimiza leitura da conversa quando a direcao remetente/destinatario esta invertida.
-- Complementa o indice sender/receiver existente para consultas bidirecionais.
CREATE INDEX idx_messages_conversation_reverse_sent_at
    ON messages (group_id, receiver_id, sender_id, data_envio);
