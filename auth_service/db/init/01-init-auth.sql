CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_password_changed_at TIMESTAMP NOT NULL
);

INSERT INTO users (username, email, password_hash, created_at, last_password_changed_at)
VALUES
    ('admin', 'admin@example.com',
     crypt('password123', gen_salt('bf', 10)), NOW(), NOW()),
    ('lukasw', 'lukas.weber@example.com',
     crypt('password123', gen_salt('bf', 10)), NOW(), NOW())
ON CONFLICT (email) DO NOTHING;