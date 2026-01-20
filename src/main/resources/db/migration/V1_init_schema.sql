CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cards (
                       id BIGSERIAL PRIMARY KEY,
                       card_number VARCHAR(255) NOT NULL UNIQUE,
                       masked_number VARCHAR(50) NOT NULL,
                       owner VARCHAR(255) NOT NULL,
                       expiry_date DATE NOT NULL,
                       status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED')),
                       balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
                       user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       CHECK (balance >= 0)
);

CREATE TABLE transfers (
                           id BIGSERIAL PRIMARY KEY,
                           from_card_id BIGINT NOT NULL REFERENCES cards(id),
                           to_card_id BIGINT NOT NULL REFERENCES cards(id),
                           amount DECIMAL(19,2) NOT NULL,
                           description VARCHAR(500),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           CHECK (amount > 0)
);

-- Индексы для производительности
CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_cards_status ON cards(status);
CREATE INDEX idx_transfers_user_id ON transfers(user_id);
CREATE INDEX idx_transfers_created_at ON transfers(created_at);