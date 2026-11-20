-- CoffeeShopApp / Moc Mien Coffee - Day 5 reusable seed
-- MySQL 8; safe to rerun: inserts only when category code / product name+category is absent.
-- Run after backend has created the tables via JPA.
-- This script does not delete or update existing rows and does not include account credentials.

USE coffee_shop;
START TRANSACTION;

INSERT INTO categories (code, name, description, available, created_at)
SELECT 'COFFEE', 'Cà phê', 'Danh mục đồ uống cà phê', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE code = 'COFFEE');

INSERT INTO categories (code, name, description, available, created_at)
SELECT 'TEA', 'Trà', 'Các loại trà trái cây và trà pha chế', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE code = 'TEA');

INSERT INTO categories (code, name, description, available, created_at)
SELECT 'JUICE', 'Nước ép', 'Các loại nước ép trái cây tươi', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE code = 'JUICE');

INSERT INTO products (name, description, price, image_url, category, available, created_at)
SELECT 'Cà phê sữa đá', 'Cà phê pha phin với sữa đặc', 35000.00,
       'https://images.unsplash.com/photo-1509042239860-f550ce710b93', 'COFFEE', 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Cà phê sữa đá' AND category = 'COFFEE'
);

INSERT INTO products (name, description, price, image_url, category, available, created_at)
SELECT 'Trà đào cam sả', 'Trà đào thơm mát kết hợp cam tươi và sả', 39000.00,
       'https://images.unsplash.com/photo-1544787219-7f47ccb76574', 'TEA', 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Trà đào cam sả' AND category = 'TEA'
);

INSERT INTO products (name, description, price, image_url, category, available, created_at)
SELECT 'Nước ép cam tươi', 'Nước cam nguyên chất, vị ngọt thanh tự nhiên', 35000.00,
       'https://images.unsplash.com/photo-1600271886742-f049cd451bba', 'JUICE', 1, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE name = 'Nước ép cam tươi' AND category = 'JUICE'
);

COMMIT;

-- Verify the seed and its references:
SELECT id, code, name, available FROM categories ORDER BY id;
SELECT id, name, price, category, available FROM products ORDER BY id;
SELECT p.id, p.name, p.category
FROM products p LEFT JOIN categories c ON p.category = c.code
WHERE c.id IS NULL;
