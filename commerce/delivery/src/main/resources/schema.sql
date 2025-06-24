CREATE TABLE IF NOT EXISTS addresses
(
    address_id UUID PRIMARY KEY,
    country    VARCHAR,
    city       VARCHAR,
    street     VARCHAR,
    house      VARCHAR,
    flat       VARCHAR
);

CREATE TABLE IF NOT EXISTS deliveries
(
    delivery_id     UUID PRIMARY KEY,
    from_address_id UUID    NOT NULL REFERENCES addresses (address_id) ON DELETE CASCADE,
    to_address_id   UUID    NOT NULL REFERENCES addresses (address_id) ON DELETE CASCADE,
    order_id        UUID    NOT NULL,
    delivery_state  VARCHAR NOT NULL
);
