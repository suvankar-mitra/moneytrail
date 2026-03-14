CREATE TABLE tags(
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tag_name VARCHAR(255) NOT NULL,
    UNIQUE (user_id, tag_name)
);
