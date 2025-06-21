CREATE TABLE IF NOT EXISTS orders
(
    order_id         UUID PRIMARY KEY,
    shopping_cart_id UUID,
    username         VARCHAR,
    payment_id       UUID,
    delivery_id      UUID,
    state            VARCHAR,
    delivery_weight  DOUBLE PRECISION,
    delivery_volume  DOUBLE PRECISION,
    fragile          BOOLEAN,
    total_price      DECIMAL,
    delivery_price   DECIMAL,
    product_price    DECIMAL
);

CREATE TABLE IF NOT EXISTS order_products
(
    order_id   UUID   NOT NULL REFERENCES orders (order_id),
    product_id UUID   NOT NULL,
    quantity   BIGINT NOT NULL,
    PRIMARY KEY (order_id, product_id)
);
