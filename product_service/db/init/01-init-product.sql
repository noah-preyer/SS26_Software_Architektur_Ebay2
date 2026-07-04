CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    category VARCHAR(255),
    seller_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS product_images (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL
);

INSERT INTO products (title, description, price, category, seller_id, status, created_at)
VALUES
    ('iPhone 14 Pro 256GB Space Black', 'Kaum genutzt, keine Kratzer, Originalzubehör vorhanden.', 749.99, 'Elektronik', 1, 'AVAILABLE', '2026-04-03 09:14:00'),
    ('Sony WH-1000XM5 Kopfhörer', 'Noise-Cancelling Bluetooth Kopfhörer, sehr guter Zustand.', 249.00, 'Elektronik', 1, 'AVAILABLE', '2026-04-17 14:32:00'),
    ('Nike Air Max 90 Gr. 43', 'Sneaker in Größe 43, kaum getragen, weiß/schwarz.', 89.50, 'Schuhe', 2, 'AVAILABLE', '2026-05-02 11:05:00'),
    ('LEGO Technic Bugatti Chiron 42083', 'Vollständig und ungeöffnet, OVP.', 319.00, 'Spielzeug', 2, 'AVAILABLE', '2026-05-21 16:48:00'),
    ('Dyson V11 Absolute Staubsauger', 'Akkustaubsauger mit allen Aufsätzen, top Zustand.', 399.00, 'Haushalt', 3, 'AVAILABLE', '2026-06-08 08:20:00'),
    ('MacBook Pro M2 14" 512GB', '2023er Modell, Space Grau, Akku 95% Kapazität.', 1499.00, 'Elektronik', 3, 'AVAILABLE', '2026-06-15 13:11:00'),
    ('Harry Potter Buchset 1-7', 'Alle 7 Bände der deutschen Ausgabe, guter Zustand.', 45.00, 'Bücher', 4, 'AVAILABLE', '2026-06-22 10:37:00'),
    ('Garmin Forerunner 255 Smartwatch', 'GPS-Laufuhr, schwarz, inkl. Ladekabel.', 199.00, 'Sport', 4, 'AVAILABLE', '2026-06-29 17:54:00')
ON CONFLICT DO NOTHING;

INSERT INTO product_images (product_id, image_url)
SELECT id, 'https://m.media-amazon.com/images/I/61cwywLZR-L._AC_UF1000,1000_QL80_.jpg' FROM products WHERE title = 'iPhone 14 Pro 256GB Space Black'
UNION ALL
SELECT id, 'https://assets.mmsrg.com/isr/166325/c1/-/ASSET_MP_147119766?x=536&y=402&format=jpg&quality=80&sp=yes&strip=yes&trim&ex=536&ey=402&align=center&resizesource&unsharp=1.5x1+0.7+0.02&cox=0&coy=0&cdx=536&cdy=402' FROM products WHERE title = 'iPhone 14 Pro 256GB Space Black'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTS2wpN_8zyEEKhKhxa-WtpVWHSTNHEIXIfi-z0wt4Acg&s=10' FROM products WHERE title = 'Sony WH-1000XM5 Kopfhörer'
UNION ALL
SELECT id, 'https://assets.mmsrg.com/isr/166325/c1/-/ASSET_MMS_161248270?x=536&y=402&format=jpg&quality=80&sp=yes&strip=yes&trim&ex=536&ey=402&align=center&resizesource&unsharp=1.5x1+0.7+0.02&cox=0&coy=0&cdx=536&cdy=402' FROM products WHERE title = 'Sony WH-1000XM5 Kopfhörer'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSLWCoi7KhO_JeNsL8uss4x3uaHmAmZD2ot6VezSBFdkQ&s=10' FROM products WHERE title = 'Sony WH-1000XM5 Kopfhörer'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTSSsY9OW78KTGqmQ1JyajRifcexJvlfSOCN_xvGV3uMw&s=10' FROM products WHERE title = 'Nike Air Max 90 Gr. 43'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQOW-vq2X4sMTvhx5zbaTVpd33TGIJhWDSK_7QL7YbC6A&s=10' FROM products WHERE title = 'LEGO Technic Bugatti Chiron 42083'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTzhG3gNmy4_o2h4DsyyOPrpFirDGlFh-fCqLq5Drxr7A&s=10' FROM products WHERE title = 'LEGO Technic Bugatti Chiron 42083'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS-mYvUfhvuNjvnxoo8vos5JTqeqnGIty2tIAXL2y3Avg&s=10' FROM products WHERE title = 'LEGO Technic Bugatti Chiron 42083'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR8emvwx9KBnSUr8-hly7zog9_-1sXU74a5daTaJiGBXw&s=10' FROM products WHERE title = 'Dyson V11 Absolute Staubsauger'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_WBAmcvFs0EKIibX8NCD-c5EfT_-77J2QA2EIhEXl8Q&s=10' FROM products WHERE title = 'Dyson V11 Absolute Staubsauger'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQk7xOEMtfiCdbapTwAz9Luw0XVHWlXpGgF4f-05sFwdw&s=10' FROM products WHERE title = 'MacBook Pro M2 14" 512GB'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRh1HPykj2Yh7hx9KQwxls-lXoQGVZFD3KajNyzT_m4NA&s=10' FROM products WHERE title = 'MacBook Pro M2 14" 512GB'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSV-3ZgMLUPojRuyxedOJbL0e3mxWfOLj_aI00kPgETfg&s=10' FROM products WHERE title = 'Harry Potter Buchset 1-7'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRFlaNCtzaK9nXdirVi6KaxI6WbtTsyJreMVyrnuu9LZw&s=10' FROM products WHERE title = 'Garmin Forerunner 255 Smartwatch'
UNION ALL
SELECT id, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSrSqoEo9P-oxzuS9991Urw2_JsY0E5gB2-McQTBt5RSQ&s=10' FROM products WHERE title = 'Garmin Forerunner 255 Smartwatch'
ON CONFLICT DO NOTHING;
