CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    auth_id VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_auth_id UNIQUE (auth_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE groups_table (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    codigo_unico VARCHAR(9) NOT NULL,
    dono_id BIGINT NOT NULL,
    data_criacao DATETIME(6),
    data_sorteio DATETIME(6),
    data_evento DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_groups_codigo_unico UNIQUE (codigo_unico),
    CONSTRAINT uk_groups_dono UNIQUE (dono_id),
    CONSTRAINT fk_groups_dono FOREIGN KEY (dono_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE group_members (
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT uk_group_member UNIQUE (group_id, user_id),
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups_table (id) ON DELETE CASCADE,
    CONSTRAINT fk_group_members_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE draws (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    giver_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_draw_group_giver UNIQUE (group_id, giver_id),
    CONSTRAINT uk_draw_group_receiver UNIQUE (group_id, receiver_id),
    CONSTRAINT fk_draws_group FOREIGN KEY (group_id) REFERENCES groups_table (id) ON DELETE CASCADE,
    CONSTRAINT fk_draws_giver FOREIGN KEY (giver_id) REFERENCES users (id),
    CONSTRAINT fk_draws_receiver FOREIGN KEY (receiver_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    conteudo TEXT NOT NULL,
    data_envio DATETIME(6),
    lida BIT(1) NOT NULL,
    anonima BIT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_messages_group FOREIGN KEY (group_id) REFERENCES groups_table (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id),
    CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    mensagem TEXT NOT NULL,
    data_criacao DATETIME(6),
    lida BIT(1) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wishlists (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    sugestao_ia TEXT,
    PRIMARY KEY (id),
    CONSTRAINT uk_wishlists_user UNIQUE (user_id),
    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wishlist_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    wishlist_id BIGINT NOT NULL,
    nome_produto VARCHAR(255) NOT NULL,
    link TEXT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_wishlist_items_wishlist FOREIGN KEY (wishlist_id) REFERENCES wishlists (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_group_members_user ON group_members (user_id);
CREATE INDEX idx_draws_giver ON draws (giver_id);
CREATE INDEX idx_draws_receiver ON draws (receiver_id);
CREATE INDEX idx_messages_group_sent_at ON messages (group_id, data_envio);
CREATE INDEX idx_messages_receiver_unread ON messages (receiver_id, lida);
CREATE INDEX idx_notifications_user_created_at ON notifications (user_id, data_criacao);
