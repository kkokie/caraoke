-- V4: blog-length stories. 2,000 chars was a tweet-ish cap; long-form memories need room.
ALTER TABLE stories ALTER COLUMN body TYPE TEXT;

-- The app enforces this too (StoryRules); the DB is the backstop.
ALTER TABLE stories ADD CONSTRAINT stories_body_length CHECK (char_length(body) BETWEEN 1 AND 10000);
