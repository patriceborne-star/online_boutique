-- Online Boutique schema for yugabyteDB

CREATE TABLE IF NOT EXISTS users (
    email                   TEXT PRIMARY KEY,
    password                TEXT NOT NULL DEFAULT 'password',
    first_name              TEXT,
    last_name               TEXT,
    street_address          TEXT,
    city                    TEXT,
    state                   TEXT,
    zip_code                TEXT,
    country                 TEXT,
    phone                   TEXT,
    credit_card_number      TEXT,
    credit_card_exp_month   INT,
    credit_card_exp_year    INT,
    credit_card_cvv         TEXT,
    created_at              TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS products (
    id                      TEXT PRIMARY KEY,
    name                    TEXT NOT NULL,
    description             TEXT,
    picture                 TEXT,
    currency_code           TEXT DEFAULT 'USD',
    price_units             BIGINT NOT NULL,
    price_nanos             INT NOT NULL DEFAULT 0,
    categories              TEXT
);

CREATE TABLE IF NOT EXISTS cart_items (
    user_id                 TEXT NOT NULL REFERENCES users(email),
    product_id              TEXT NOT NULL REFERENCES products(id),
    quantity                INT NOT NULL DEFAULT 1,
    PRIMARY KEY (user_id, product_id)
);

CREATE TABLE IF NOT EXISTS orders (
    order_id                TEXT PRIMARY KEY,
    user_id                 TEXT NOT NULL REFERENCES users(email),
    user_currency           TEXT DEFAULT 'USD',
    email                   TEXT,
    street_address          TEXT,
    city                    TEXT,
    state                   TEXT,
    zip_code                TEXT,
    country                 TEXT,
    shipping_cost_units     BIGINT DEFAULT 0,
    shipping_cost_nanos     INT DEFAULT 0,
    shipping_tracking_id    TEXT,
    total_units             BIGINT DEFAULT 0,
    total_nanos             INT DEFAULT 0,
    status                  TEXT DEFAULT 'pending',
    created_at              TIMESTAMPTZ DEFAULT now(),
    shipped_at              TIMESTAMPTZ,
    delivered_at            TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS order_items (
    order_id                TEXT NOT NULL REFERENCES orders(order_id),
    product_id              TEXT NOT NULL REFERENCES products(id),
    quantity                INT NOT NULL,
    cost_units              BIGINT NOT NULL,
    cost_nanos              INT NOT NULL DEFAULT 0,
    PRIMARY KEY (order_id, product_id)
);
