CREATE TABLE transactions(
    id UUID PRIMARY KEY,
    from_account_id UUID NOT NULL CONSTRAINT fk_transactions_from_account REFERENCES accounts(id) ON DELETE RESTRICT,
    to_account_id UUID NOT NULL CONSTRAINT fk_transactions_to_account REFERENCES accounts(id) ON DELETE RESTRICT,
    transaction_amount DECIMAL(19, 4) NOT NULL CONSTRAINT ck_transactions_amount CHECK(transaction_amount > 0),
    exchange_rate DECIMAL(18, 6) NOT NULL DEFAULT 1.0 CONSTRAINT ck_transactions_xch_ratio CHECK(exchange_rate > 0),
    tran_date DATE DEFAULT CURRENT_DATE NOT NULL,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX transactions_idx_from_account_id ON transactions(from_account_id);
CREATE INDEX transactions_idx_to_account_id ON transactions(to_account_id);
