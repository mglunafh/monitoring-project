INSERT INTO suppliers(name, description) VALUES
    ('ChemPharm', 'Производство лекарственных препаратов и медицинских товаров'),
    ('NanoTech', 'Создание и обслуживание боевых роботов'),
    ('AlgolBreadProd', 'АлголХлебПром -- завод злебобулочных и кондитерских изделий'),
    ('Milky Farm', 'Сеть фермерских хозяйств, поставляет молоко и молочную продукцию'),
    ('Dairy Factory', 'Молокозавод полного цикла, широкий профиль молочной продукции'),
    ('Agrifield', 'Фрукты'),
    ('Bepis Distro Ltd', 'Bepis'),
    ('Pindonnier Gardens', 'Сады Пиндонье, производство соков защищенного географического положения'),
    ('Liquid.Gold', 'Завод традиционных газированных напитков'),
    ('Chuson', 'Мониторы бюджетного плана'),
    ('LerX', 'Премиум-производитель электронной техники высокого качества'),
    ('GigaSan', 'Сантехнические товары');

-- drugs
INSERT INTO goods(name, category, amount, weight) VALUES
    ('Nurofen', 'MEDICINE', 0, 0.010),
    ('Mucaltin', 'MEDICINE', 0, 0.010),
    ('Stopangin', 'MEDICINE', 0, 0.010),
    ('Suprastin', 'MEDICINE', 0, 0.010),
    ('Activated charcoal', 'MEDICINE', 0, 0.010),
    ('Noshpa', 'MEDICINE', 0, 0.010),
    ('Visin', 'MEDICINE', 0, 0.010),
    ('Zodak', 'MEDICINE', 0, 0.010),
    ('Amoxicilline', 'MEDICINE', 0, 0.030),
    ('Pregnancy test', 'MEDICINE', 0, 0.050);

-- bakery
INSERT INTO goods(name, category, amount, weight) VALUES
    ('Baton', 'BAKERY', 0, 0.400),
    ('Bread', 'BAKERY', 0, 0.400),
    ('Buns', 'BAKERY', 0, 0.250),
    ('Raisin muffin', 'BAKERY', 0, 0.150),
    ('Chocolate cookies', 'BAKERY', 0, 0.500);

-- Dairy
INSERT INTO goods(name, category, amount, weight) VALUES
    ('Milk 1.5%', 'DAIRY', 0, 1.000),
    ('Milk 2.5%', 'DAIRY', 0, 1.000),
    ('Milk 3.2%', 'DAIRY', 0, 1.000),
    ('Yoghurt', 'DAIRY', 0, 0.200),
    ('Sour cream', 'DAIRY', 0, 0.330),
    ('Cheese', 'DAIRY', 0, 0.500),
    ('Cream cheese', 'DAIRY', 0, 0.220),
    ('Butter', 'DAIRY', 0, 0.200);

-- Fruits
INSERT INTO goods(name, category, amount, weight) VALUES
    ('Pineapple', 'FRUIT', 0, 2.000),
    ('Watermelon', 'FRUIT', 0, 8.000),
    ('Melon', 'FRUIT', 0, 6.000),
    ('Pumpkin', 'FRUIT', 0, 5.000);

-- Beverages
INSERT INTO goods(name, category, amount, weight) VALUES
    ('Bepis 0.5', 'BEVERAGE', 0, 0.500),
    ('Bepis 1.0', 'BEVERAGE', 0, 1.000),
    ('Bepis 2.0', 'BEVERAGE', 0, 2.000),
    ('Apple Juice', 'BEVERAGE', 0, 1.000),
    ('Orange juice', 'BEVERAGE', 0, 1.000),
    ('Kvass', 'BEVERAGE', 0, 1.500);

-- Monitors
INSERT INTO goods(name, category, amount, weight) VALUES
    ('Chuson BRF17HD', 'MONITOR', 0, 3.500),
    ('Chuson BRF21HD', 'MONITOR', 0, 4.200),
    ('Chuson BRF24HD', 'MONITOR', 0, 6.900),
    ('LerX QLPL24QHD', 'MONITOR', 0, 5.100),
    ('LerX QLPL27QHD', 'MONITOR', 0, 5.300),
    ('LerX QLPL31QHD', 'MONITOR', 0, 7.200);

-- Sanitary stuff
INSERT INTO goods(name, category, amount, weight) VALUES
    ('Acrylic bathtub', 'SANITARY', 0, 19.000),
    ('Sink', 'SANITARY', 0, 10.000),
    ('Sink with cabinet', 'SANITARY', 0, 19.000),
    ('Toilet', 'SANITARY', 0, 27.000);

-- War robots
INSERT INTO goods(name, category, amount, weight) VALUES
    ('Giant Humanlike War Robot', 'MECH', 0, 2000.000),
    ('Small War Robot', 'MECH', 0, 230.000);
