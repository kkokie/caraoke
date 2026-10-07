-- V6: "stories I felt", newest first, keyset-paged by (created_at, story_id).
CREATE INDEX idx_resonances_user_recent ON resonances (user_id, created_at DESC, story_id DESC);
