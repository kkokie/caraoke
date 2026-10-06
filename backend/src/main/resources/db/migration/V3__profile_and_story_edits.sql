-- V3: profile dashboard + story editing.

-- When a story was last edited (null = never). Shown as "edited" in the app.
ALTER TABLE stories ADD COLUMN edited_at TIMESTAMPTZ;

-- Keyset paging of one user's visible stories for the profile grid.
CREATE INDEX idx_stories_user_feed ON stories (user_id, id DESC) WHERE status = 'VISIBLE';
