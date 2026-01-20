-- Добавляем каскадное удаление для связанных записей
ALTER TABLE cards
DROP CONSTRAINT IF EXISTS cards_user_id_fkey,
ADD CONSTRAINT cards_user_id_fkey
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE transfers
DROP CONSTRAINT IF EXISTS transfers_user_id_fkey,
ADD CONSTRAINT transfers_user_id_fkey
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Добавляем проверку на положительный баланс
ALTER TABLE cards
ADD CONSTRAINT positive_balance CHECK (balance >= 0);

-- Добавляем уникальность для email (если еще нет)
ALTER TABLE users
ADD CONSTRAINT unique_email UNIQUE (email);

-- Создаем индекс для быстрого поиска по email
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Создаем индекс для поиска карт по владельцу
CREATE INDEX IF NOT EXISTS idx_cards_owner ON cards(owner);

-- Создаем индекс для поиска переводов по дате
CREATE INDEX IF NOT EXISTS idx_transfers_date ON transfers(created_at DESC);