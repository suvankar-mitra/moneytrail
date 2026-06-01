CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL CONSTRAINT fk_accounts_user_id REFERENCES users(id) ON DELETE CASCADE,
    contact_id UUID CONSTRAINT fk_accounts_contact_id REFERENCES contacts(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL CONSTRAINT ck_accounts_type CHECK (type IN('ASSET','LIABILITY', 'INCOME', 'EXPENSE', 'INVESTMENT', 'RECEIVABLE', 'PAYABLE', 'OPENING_BALANCE_EQUITY')),
    currency CHAR(3) NOT NULL,
    is_virtual BOOLEAN NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' CONSTRAINT ck_accounts_status CHECK(status IN('ACTIVE', 'DELETED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX accounts_idx_user_id ON accounts(user_id);

CREATE UNIQUE INDEX uq_accounts_equity_user_currency 
ON accounts (user_id, currency) 
WHERE type = 'OPENING_BALANCE_EQUITY';
