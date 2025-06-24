CREATE TABLE IF NOT EXISTS payments
(
    payment_id     UUID PRIMARY KEY,
    order_id       UUID    NOT NULL,
    total_payment  DECIMAL,
    delivery_total DECIMAL,
    fee_total      DECIMAL,
    payment_state  VARCHAR NOT NULL
);
