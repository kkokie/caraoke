-- V5: followers / following.
CREATE TABLE follows (
    follower_id BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    followee_id BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (follower_id, followee_id),
    CHECK (follower_id <> followee_id)
);

-- "Who follows X" and "who does X follow", newest first.
CREATE INDEX idx_follows_followee_recent ON follows (followee_id, created_at DESC);
CREATE INDEX idx_follows_follower_recent ON follows (follower_id, created_at DESC);
