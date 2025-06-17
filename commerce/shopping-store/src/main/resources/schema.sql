CREATE TABLE IF NOT EXISTS products
(
    product_id       UUID PRIMARY KEY,
    product_name     VARCHAR NOT NULL,
    description      VARCHAR NOT NULL,
    image_src        VARCHAR,
    quantity_state   VARCHAR NOT NULL,
    product_state    VARCHAR NOT NULL,
    product_category VARCHAR,
    price            REAL
);
