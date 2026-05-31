CREATE TABLE exchange_rates(
    business_date DATE,
    base_currency VARCHAR(10) NOT NULL,
    to_currency VARCHAR(10) NOT NULL,
    exchange_rate DECIMAL(18, 6) NOT NULL DEFAULT 1.0 CONSTRAINT ck_exchange_rates_exchange_rate CHECK(exchange_rate > 0),
    fetched_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    source VARCHAR(255) NOT NULL,
    CONSTRAINT pk_exchange_rates PRIMARY KEY (business_date, base_currency, to_currency)
);
