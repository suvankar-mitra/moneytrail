CREATE TABLE transactions(
    id UUID PRIMARY KEY,
    from_account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    to_account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    amount DECIMAL(19, 4) NOT NULL CHECK(amount > 0),
    tran_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX transactions_idx_from_account_id ON transactions(from_account_id);
CREATE INDEX transactions_idx_to_account_id ON transactions(to_account_id);
