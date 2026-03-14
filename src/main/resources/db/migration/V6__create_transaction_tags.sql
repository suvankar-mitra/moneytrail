CREATE TABLE transaction_tags(
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    UNIQUE(transaction_id, tag_id)
);

CREATE INDEX transaction_tags_idx_transaction_id ON transaction_tags(transaction_id);
CREATE INDEX transaction_tags_idx_tag_id ON transaction_tags(tag_id);
