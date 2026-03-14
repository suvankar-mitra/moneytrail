CREATE TABLE transaction_tags(
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    tag_id BIGINT NOT NULL REFERENCES tags(id),
    UNIQUE(transaction_id, tag_id)
);
