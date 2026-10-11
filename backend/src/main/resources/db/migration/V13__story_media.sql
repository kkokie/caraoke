-- V13: photos on stories. The DB stores object-storage keys only, never blobs or URLs.
-- Many per story in the schema (ordered by position); the app allows one for now.
CREATE TABLE story_media (
    id           BIGSERIAL    PRIMARY KEY,
    story_id     BIGINT       NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    position     SMALLINT     NOT NULL DEFAULT 0 CHECK (position >= 0),
    storage_key  VARCHAR(255) NOT NULL,
    width        INT          CHECK (width > 0),
    height       INT          CHECK (height > 0),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (story_id, position)
);
