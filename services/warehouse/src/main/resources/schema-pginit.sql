CREATE SCHEMA IF NOT EXISTS warehouse;

CREATE TABLE IF NOT EXISTS warehouse.suppliers (
    id smallserial PRIMARY KEY,
    name varchar(30) UNIQUE NOT NULL,
    description varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS warehouse.goods (
    id smallserial PRIMARY KEY,
    name varchar(30) UNIQUE NOT NULL,
    category varchar(20) NOT NULL,
    amount integer NOT NULL CHECK (amount >= 0),
    weight numeric(8, 3) NOT NULL CHECK (weight > 0)
);

CREATE TABLE IF NOT EXISTS warehouse.supply_contracts (
    id serial PRIMARY KEY,
    supplier_id smallint REFERENCES warehouse.suppliers(id),
    sign_date timestamp(6) without time zone NOT NULL,
    price numeric(15,2) NOT NULL CHECK (price > 0)
);

CREATE TABLE IF NOT EXISTS warehouse.goods_in_contract (
    id bigserial PRIMARY KEY,
    contract_id integer REFERENCES warehouse.supply_contracts(id),
    item_id smallint REFERENCES warehouse.goods(id),
    amount integer NOT NULL CHECK (amount > 0)
);

CREATE TABLE IF NOT EXISTS warehouse.goods_reserve (
    id bigserial PRIMARY KEY,
    shopping_cart_id varchar(255) NOT NULL,
    item_id smallint REFERENCES warehouse.goods(id),
    amount integer NOT NULL CHECK (amount > 0),
    action_time timestamp(6) without time zone NOT NULL,
    status varchar(20) NOT NULL
);
