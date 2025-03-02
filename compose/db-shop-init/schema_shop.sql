CREATE SCHEMA shop;

-- Users
CREATE TABLE IF NOT EXISTS shop.customers(
    id serial PRIMARY KEY,
    login varchar(30) UNIQUE NOT NULL,
    registered_at timestamp(6) without time zone NOT NULL,
    password varchar(70) NOT NULL
)

-- Items

-- Shopping carts

-- Items in shopping cart
