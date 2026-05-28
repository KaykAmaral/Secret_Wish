CREATE INDEX idx_messages_conversation_read
    ON messages (group_id, receiver_id, sender_id, lida);

CREATE INDEX idx_messages_conversation_sent_at
    ON messages (group_id, sender_id, receiver_id, data_envio);

CREATE INDEX idx_notifications_user_read
    ON notifications (user_id, lida);

CREATE INDEX idx_wishlist_items_wishlist
    ON wishlist_items (wishlist_id);
