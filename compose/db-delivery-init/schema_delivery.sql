CREATE SCHEMA delivery;

-- Transport Type
CREATE TABLE delivery.transport_type (
    id integer NOT NULL,
    category character varying(255),
    mark character varying(255),
    max_cargo integer NOT NULL,
    max_distance integer NOT NULL,
    price_per_distance numeric(38,2),
    speed integer NOT NULL
);

CREATE SEQUENCE delivery.transport_type_seq
    START WITH 1
    INCREMENT BY 7
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE ONLY delivery.transport_type
    ADD CONSTRAINT transport_type_pkey PRIMARY KEY (id);

ALTER TABLE ONLY delivery.transport_type
    ADD CONSTRAINT ukk9d0ookevu0j1j6os7ayiah67 UNIQUE (mark);


-- Transport Park
CREATE TABLE delivery.park (
    id integer NOT NULL,
    status character varying(255),
    transport_type_id integer
);

CREATE SEQUENCE delivery.park_seq
    START WITH 1
    INCREMENT BY 5
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE ONLY delivery.park
    ADD CONSTRAINT park_pkey PRIMARY KEY (id);

ALTER TABLE ONLY delivery.park
    ADD CONSTRAINT fkmgs4k6xl696ptdickco4o1n3 FOREIGN KEY (transport_type_id) REFERENCES delivery.transport_type(id);


-- Delivery Order
CREATE TABLE delivery.delivery_order (
    id integer NOT NULL,
    arrival_time timestamp(6) without time zone,
    cost numeric(38,2),
    departure_time timestamp(6) without time zone,
    distance integer NOT NULL,
    order_time timestamp(6) without time zone,
    shopping_cart_id character varying(255),
    status character varying(255),
    transport_id integer,
    transport_type_id integer
);

CREATE SEQUENCE delivery.delivery_order_seq
    START WITH 1
    INCREMENT BY 3
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE ONLY delivery.delivery_order
    ADD CONSTRAINT delivery_order_pkey PRIMARY KEY (id);

ALTER TABLE ONLY delivery.delivery_order
    ADD CONSTRAINT fka28vs16h3q37qt6my8q02vx4d FOREIGN KEY (transport_type_id) REFERENCES delivery.transport_type(id);

ALTER TABLE ONLY delivery.delivery_order
    ADD CONSTRAINT fkdqxabydps584nkgqrmjpgbbhx FOREIGN KEY (transport_id) REFERENCES delivery.park(id);

ALTER TABLE ONLY delivery.delivery_order
    ADD CONSTRAINT uktoknbwi176loxq9n0n3gq1x27 UNIQUE (shopping_cart_id);
