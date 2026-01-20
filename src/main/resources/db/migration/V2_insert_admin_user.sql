INSERT INTO users (email, password, first_name, last_name, role, created_at)
VALUES (
           'admin@bank.com',
           '$2a$10$Beh/ZvjNgGeJTNQHKpDV0.6hk71K1WJFr3PMW6ns9hrqSvlBOwMym',
           'Admin',
           'System',
           'ADMIN',
           CURRENT_TIMESTAMP
       )
    ON CONFLICT (email) DO NOTHING;