CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_password_changed_at TIMESTAMP NOT NULL
);

INSERT INTO users (id, username, email, password_hash, created_at, last_password_changed_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'admin', 'admin@example.com',
     crypt('password123', gen_salt('bf', 10)), NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', 'lukasw', 'lukas.weber@example.com',
     crypt('password123', gen_salt('bf', 10)), NOW(), NOW())
ON CONFLICT (email) DO NOTHING;