CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS roles (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT REFERENCES roles(id),
    created_at TIMESTAMP NOT NULL,
    last_password_changed_at TIMESTAMP NOT NULL
    );

INSERT INTO roles (name)
VALUES
    ('ADMIN'),
    ('USER')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO users (
    id,
    username,
    email,
    password_hash,
    role_id,
    created_at,
    last_password_changed_at
)
VALUES
    (
        '11111111-1111-1111-1111-111111111111',
        'admin',
        'admin@example.com',
        '$2y$10$a6nPWSEVHozK4MNcy.2wCOHbbPfGMwuxZl13GTRd/qUu3d.juJheG',
        (SELECT id FROM roles WHERE name = 'ADMIN'),
        NOW(),
        NOW()
    ),
    (
        '22222222-2222-2222-2222-222222222222',
        'lukasw',
        'lukas.weber@example.com',
        '$2y$10$caGNCMDpQ5HSoQbgWCVEheeYJQ3IbUF0/CFsc3znNBHW38fPIh5FK',
        (SELECT id FROM roles WHERE name = 'USER'),
        NOW(),
        NOW()
    )
    ON CONFLICT (email) DO NOTHING;