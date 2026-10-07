-- V8: the share flow. The lyric line someone felt (typed from memory, kept short so it stays
-- a quote, never full lyrics) and the paper color their story card is printed on.
ALTER TABLE stories ADD COLUMN lyric_quote VARCHAR(120);
ALTER TABLE stories ADD COLUMN paper VARCHAR(16) NOT NULL DEFAULT 'CREAM';
ALTER TABLE stories ADD CONSTRAINT stories_paper_known
    CHECK (paper IN ('CREAM', 'DUSK', 'SAGE', 'INK', 'ROSE', 'TAPE'));

-- One story a day: "when did this person last share?" is a lookup on (user_id, created_at).
CREATE INDEX idx_stories_user_created ON stories (user_id, created_at DESC);
