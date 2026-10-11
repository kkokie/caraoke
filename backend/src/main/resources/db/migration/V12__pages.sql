-- V12: your page, decorated (Cyworld minihompy spirit). Curated options only: every value is a
-- key the app knows how to draw, never raw HTML or colors, so pages stay beautiful and fast.
CREATE TABLE pages (
    user_id          BIGINT       PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    theme            VARCHAR(16)  NOT NULL DEFAULT 'PAPER',
    background       VARCHAR(16)  NOT NULL DEFAULT 'PLAIN',
    type_pairing     VARCHAR(16)  NOT NULL DEFAULT 'CLASSIC',
    page_song_id     BIGINT       REFERENCES songs(id) ON DELETE SET NULL,
    pinned_story_id  BIGINT       REFERENCES stories(id) ON DELETE SET NULL,
    blocks           VARCHAR(200) NOT NULL DEFAULT 'PAGE_SONG,PINNED_STORY,LIFE_IN_SONGS,RAISED_ME',  -- shown, in order
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- "Songs that raised me": up to 5, in the order you put them.
CREATE TABLE page_raised_songs (
    user_id   BIGINT   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    position  SMALLINT NOT NULL CHECK (position BETWEEN 0 AND 4),
    song_id   BIGINT   NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, position)
);

-- "My life in songs": an author's stories by the year the memory is from
CREATE INDEX idx_stories_user_year ON stories (user_id, year_of_memory) WHERE status = 'VISIBLE' AND year_of_memory IS NOT NULL;
