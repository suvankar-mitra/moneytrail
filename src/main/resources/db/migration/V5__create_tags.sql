CREATE TABLE tags(
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL CONSTRAINT fk_tags_user_id REFERENCES users(id) ON DELETE CASCADE,
    tag_name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_tags_user_id_tag_name UNIQUE (user_id, tag_name)
);
