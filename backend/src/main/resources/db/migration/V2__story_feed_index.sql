-- V2: keyset pagination for a song's story feed.
-- The feed reads "newest visible stories for song X older than cursor id".
-- A partial index on (song_id, id DESC) over visible rows makes that an index range scan.
CREATE INDEX idx_stories_song_feed ON stories (song_id, id DESC) WHERE status = 'VISIBLE';
