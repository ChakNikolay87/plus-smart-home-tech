CREATE TABLE IF NOT EXISTS carts
(
    shopping_cart_id UUID PRIMARY KEY,
    username         VARCHAR NOT NULL,
    is_active        BOOLEAN
);

CREATE TABLE IF NOT EXISTS cart_products
(
    cart_id    UUID   NOT NULL REFERENCES carts (shopping_cart_id),
    product_id UUID   NOT NULL,
    quantity   BIGINT NOT NULL,
    PRIMARY KEY (cart_id, product_id)
);
