CREATE TABLE transaction_tags(
    transaction_id UUID NOT NULL CONSTRAINT fk_transaction_tags_transaction_id REFERENCES transactions(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL CONSTRAINT fk_transaction_tags_tag_id REFERENCES tags(id) ON DELETE CASCADE,
    CONSTRAINT uq_transaction_tags_transaction_id_tag_id UNIQUE(transaction_id, tag_id)
);

CREATE INDEX transaction_tags_idx_transaction_id ON transaction_tags(transaction_id);
CREATE INDEX transaction_tags_idx_tag_id ON transaction_tags(tag_id);
