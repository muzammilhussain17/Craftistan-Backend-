-- V1: Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id           BIGSERIAL PRIMARY KEY,
    product_id   BIGINT       NOT NULL,
    user_id      VARCHAR(255) NOT NULL,
    user_name    VARCHAR(255),
    user_avatar  VARCHAR(255),
    rating       INTEGER      NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment      VARCHAR(1000),
    helpful      INTEGER      NOT NULL DEFAULT 0,
    verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    is_hidden    BOOLEAN      NOT NULL DEFAULT FALSE,
    is_flagged   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    CONSTRAINT uq_product_user_review UNIQUE (product_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON reviews (product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id    ON reviews (user_id);
