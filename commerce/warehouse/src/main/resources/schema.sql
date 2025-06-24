CREATE TABLE IF NOT EXISTS warehouse_products
(
    product_id UUID PRIMARY KEY,
    fragile    BOOLEAN,
    width      DOUBLE PRECISION NOT NULL,
    height     DOUBLE PRECISION NOT NULL,
    depth      DOUBLE PRECISION NOT NULL,
    weight     DOUBLE PRECISION NOT NULL,
    quantity   BIGINT
);

CREATE TABLE IF NOT EXISTS order_bookings
(
    order_booking_id UUID PRIMARY KEY,
    order_id         UUID             NOT NULL,
    delivery_id      UUID,
    fragile          BOOLEAN,
    delivery_volume  DOUBLE PRECISION NOT NULL,
    delivery_weight  DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS order_booking_products
(
    order_booking_id UUID REFERENCES order_bookings (order_booking_id) ON DELETE CASCADE,
    product_id       UUID REFERENCES warehouse_products (product_id) ON DELETE CASCADE,
    quantity         BIGINT NOT NULL
);