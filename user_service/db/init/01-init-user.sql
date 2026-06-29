CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,

    -- entspricht users.id in auth_service (gleiche numerische id, separate datenbank).
    auth_user_id BIGINT NOT NULL UNIQUE,

    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,

    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(30),

    profile_image_object_key VARCHAR(500),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS address_types (
    code VARCHAR(50) PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    street VARCHAR(150) NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,

    address_type_code VARCHAR(50) NOT NULL REFERENCES address_types(code),
    default_address BOOLEAN NOT NULL DEFAULT FALSE,

    user_profile_id BIGINT NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

INSERT INTO address_types (code, display_name)
VALUES
    ('SHIPPING', 'Shipping'),
    ('BILLING', 'Billing'),
    ('PRIMARY', 'Primary')
ON CONFLICT (code) DO NOTHING;

-- auth_user_id 1/2 müssen mit den Seed-IDs in auth_db/init/01-init-auth.sql übereinstimmen.
INSERT INTO user_profiles (
    auth_user_id,
    username,
    email,
    first_name,
    last_name,
    phone_number,
    profile_image_object_key,
    created_at,
    updated_at
)
VALUES
    (
        1,
        'admin',
        'admin@example.com',
        'Admin',
        'User',
        '+491111111111',
        NULL,
        NOW(),
        NOW()
    ),
    (
        2,
        'lukasw',
        'lukas.weber@example.com',
        'Lukas',
        'Weber',
        '+492222222222',
        NULL,
        NOW(),
        NOW()
    )
    ON CONFLICT (email) DO NOTHING;

SELECT setval(pg_get_serial_sequence('user_profiles', 'id'), GREATEST((SELECT MAX(id) FROM user_profiles), 1));

INSERT INTO addresses (
    street, house_number, postal_code, city, country,
    address_type_code, default_address, user_profile_id, created_at, updated_at
)
VALUES
    (
        'Main Street', '1', '10115', 'Berlin', 'Germany',
        'PRIMARY', TRUE,
        (SELECT id FROM user_profiles WHERE auth_user_id = 1),
        NOW(), NOW()
    ),
    (
        'Hauptstraße', '42', '50667', 'Cologne', 'Germany',
        'SHIPPING', TRUE,
        (SELECT id FROM user_profiles WHERE auth_user_id = 2),
        NOW(), NOW()
    ),
    (
        'Bahnhofstraße', '10', '80331', 'Munich', 'Germany',
        'BILLING', FALSE,
        (SELECT id FROM user_profiles WHERE auth_user_id = 2),
        NOW(), NOW()
    )
    ON CONFLICT DO NOTHING;