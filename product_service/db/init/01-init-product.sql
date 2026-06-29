CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    category VARCHAR(255),
    seller_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS product_images (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL
);

INSERT INTO products (title, description, price, category, seller_id, quantity, status)
VALUES
    ('iPhone 14 Pro 256GB Space Black', 'Kaum genutzt, keine Kratzer, Originalzubehör vorhanden.', 749.99, 'Elektronik', 1, 5, 'AVAILABLE'),
    ('Sony WH-1000XM5 Kopfhörer', 'Noise-Cancelling Bluetooth Kopfhörer, sehr guter Zustand.', 249.00, 'Elektronik', 1, 5, 'AVAILABLE'),
    ('Nike Air Max 90 Gr. 43', 'Sneaker in Größe 43, kaum getragen, weiß/schwarz.', 89.50, 'Schuhe', 2, 5, 'AVAILABLE'),
    ('LEGO Technic Bugatti Chiron 42083', 'Vollständig und ungeöffnet, OVP.', 319.00, 'Spielzeug', 2, 5, 'AVAILABLE'),
    ('Dyson V11 Absolute Staubsauger', 'Akkustaubsauger mit allen Aufsätzen, top Zustand.', 399.00, 'Haushalt', 3, 5, 'AVAILABLE'),
    ('MacBook Pro M2 14" 512GB', '2023er Modell, Space Grau, Akku 95% Kapazität.', 1499.00, 'Elektronik', 3, 5, 'AVAILABLE'),
    ('Harry Potter Buchset 1-7', 'Alle 7 Bände der deutschen Ausgabe, guter Zustand.', 45.00, 'Bücher', 4, 5, 'AVAILABLE'),
    ('Garmin Forerunner 255 Smartwatch', 'GPS-Laufuhr, schwarz, inkl. Ladekabel.', 199.00, 'Sport', 4, 5, 'AVAILABLE')
ON CONFLICT DO NOTHING;

INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://picsum.photos/seed/iphone14pro/400/300' FROM products WHERE title = 'iPhone 14 Pro 256GB Space Black'
UNION ALL
SELECT id, 'https://picsum.photos/seed/sonywh1000xm5/400/300' FROM products WHERE title = 'Sony WH-1000XM5 Kopfhörer'
UNION ALL
SELECT id, 'https://picsum.photos/seed/nikeairmax90/400/300' FROM products WHERE title = 'Nike Air Max 90 Gr. 43'
UNION ALL
SELECT id, 'https://picsum.photos/seed/legotechnicbugatti/400/300' FROM products WHERE title = 'LEGO Technic Bugatti Chiron 42083'
UNION ALL
SELECT id, 'https://picsum.photos/seed/dysonv11/400/300' FROM products WHERE title = 'Dyson V11 Absolute Staubsauger'
UNION ALL
SELECT id, 'https://picsum.photos/seed/macbookprom2/400/300' FROM products WHERE title = 'MacBook Pro M2 14" 512GB'
UNION ALL
SELECT id, 'https://picsum.photos/seed/harrypotter/400/300' FROM products WHERE title = 'Harry Potter Buchset 1-7'
UNION ALL
SELECT id, 'https://picsum.photos/seed/garminforerunner255/400/300' FROM products WHERE title = 'Garmin Forerunner 255 Smartwatch'
ON CONFLICT DO NOTHING;
