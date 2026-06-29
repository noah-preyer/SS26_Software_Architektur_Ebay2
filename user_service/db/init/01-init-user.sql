CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    auth_user_id UUID NOT NULL UNIQUE,

    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30) NOT NULL,

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
    id BIGSERIAL PRIMARY KEY,

    street VARCHAR(150) NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,

    address_type_code VARCHAR(50) NOT NULL REFERENCES address_types(code),
    default_address BOOLEAN NOT NULL DEFAULT FALSE,

    user_profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

INSERT INTO address_types (code, display_name)
VALUES
    ('SHIPPING', 'Shipping'),
    ('BILLING', 'Billing'),
    ('PRIMARY', 'Primary')
ON CONFLICT (code) DO NOTHING;

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
        '11111111-1111-1111-1111-111111111111',
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
        '22222222-2222-2222-2222-222222222222',
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

INSERT INTO addresses (
    street, house_number, postal_code, city, country,
    address_type_code, default_address, user_profile_id, created_at, updated_at
)
VALUES
    (
        'Main Street', '1', '10115', 'Berlin', 'Germany',
        'PRIMARY', TRUE,
        (SELECT id FROM user_profiles WHERE auth_user_id = '11111111-1111-1111-1111-111111111111'),
        NOW(), NOW()
    ),
    (
        'Hauptstraße', '42', '50667', 'Cologne', 'Germany',
        'SHIPPING', TRUE,
        (SELECT id FROM user_profiles WHERE auth_user_id = '22222222-2222-2222-2222-222222222222'),
        NOW(), NOW()
    ),
    (
        'Bahnhofstraße', '10', '80331', 'Munich', 'Germany',
        'BILLING', FALSE,
        (SELECT id FROM user_profiles WHERE auth_user_id = '22222222-2222-2222-2222-222222222222'),
        NOW(), NOW()
    )
    ON CONFLICT DO NOTHING;