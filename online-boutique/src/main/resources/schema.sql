-- Online Boutique schema for Oracle

DROP TABLE users;

CREATE TABLE IF NOT EXISTS users
(
    email                 VARCHAR2(64) PRIMARY KEY,
    password              VARCHAR2(64)             DEFAULT 'password' NOT NULL,
    first_name            VARCHAR2(64),
    last_name             VARCHAR2(64),
    street_address        VARCHAR2(64),
    city                  VARCHAR2(64),
    state                 VARCHAR2(64),
    zip_code              VARCHAR2(64),
    country               VARCHAR2(64),
    phone                 VARCHAR2(64),
    credit_card_number    VARCHAR2(64),
    credit_card_exp_month INT,
    credit_card_exp_year  INT,
    credit_card_cvv       VARCHAR2(64),
    created_at            TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP
);

DROP TABLE products;

CREATE TABLE IF NOT EXISTS products
(
    id            VARCHAR2(64) PRIMARY KEY,
    name          VARCHAR2(64)           NOT NULL,
    description   VARCHAR2(64),
    picture       VARCHAR2(64),
    currency_code VARCHAR2(64) DEFAULT 'USD',
    price_units   NUMBER                 NOT NULL,
    price_nanos   INT          DEFAULT 0 NOT NULL,
    categories    VARCHAR2(64)
);

DROP TABLE cart_items;

CREATE TABLE IF NOT EXISTS cart_items
(
    user_id    VARCHAR2(64)  NOT NULL REFERENCES users (email),
    product_id VARCHAR2(64)  NOT NULL REFERENCES products (id),
    quantity   INT DEFAULT 1 NOT NULL,
    PRIMARY KEY (user_id, product_id)
);

DROP TABLE orders;

CREATE TABLE IF NOT EXISTS orders
(
    order_id             VARCHAR2(64) PRIMARY KEY,
    user_id              VARCHAR2(64) NOT NULL REFERENCES users (email),
    user_currency        VARCHAR2(64)             DEFAULT 'USD',
    email                VARCHAR2(64),
    street_address       VARCHAR2(64),
    city                 VARCHAR2(64),
    state                VARCHAR2(64),
    zip_code             VARCHAR2(64),
    country              VARCHAR2(64),
    shipping_cost_units  NUMBER                   DEFAULT 0,
    shipping_cost_nanos  INT                      DEFAULT 0,
    shipping_tracking_id VARCHAR2(64),
    total_units          NUMBER                   DEFAULT 0,
    total_nanos          INT                      DEFAULT 0,
    status               VARCHAR2(64)             DEFAULT 'pending',
    created_at           TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP,
    shipped_at           TIMESTAMP WITH TIME ZONE,
    delivered_at         TIMESTAMP WITH TIME ZONE
);

DROP TABLE order_items;

CREATE TABLE IF NOT EXISTS order_items
(
    order_id   VARCHAR2(64)  NOT NULL REFERENCES orders (order_id),
    product_id VARCHAR2(64)  NOT NULL REFERENCES products (id),
    quantity   INT           NOT NULL,
    cost_units NUMBER        NOT NULL,
    cost_nanos INT DEFAULT 0 NOT NULL,
    PRIMARY KEY (order_id, product_id)
);
