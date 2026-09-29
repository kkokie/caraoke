-- V1: core schema for the MVP.
-- Public profiles: every post is tied to a unique, visible @handle.

-- ---------------------------------------------------------------
-- Users (public profiles)
-- ---------------------------------------------------------------
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    auth_uid      VARCHAR(128) NOT NULL UNIQUE,         -- Firebase UID (subject of the JWT)
    handle        VARCHAR(20)  NOT NULL UNIQUE,         -- stored lowercase, like Instagram
    display_name  VARCHAR(50)  NOT NULL,
    bio           VARCHAR(280),
    avatar_url    VARCHAR(512),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT handle_format CHECK (handle ~ '^[a-z0-9_]{3,20}$')
);

-- ---------------------------------------------------------------
-- Songs (our own canonical catalog; platform IDs are just links)
-- ---------------------------------------------------------------
CREATE TABLE songs (
    id             BIGSERIAL PRIMARY KEY,
    isrc           VARCHAR(12) UNIQUE,
    title          TEXT        NOT NULL,
    artist         TEXT        NOT NULL,
    album          TEXT,
    album_art_url  TEXT,
    duration_sec   INT,
    apple_id       TEXT UNIQUE,
    spotify_uri    TEXT UNIQUE,
    youtube_id     TEXT UNIQUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------
-- Stories
-- ---------------------------------------------------------------
CREATE TABLE stories (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    song_id         BIGINT      NOT NULL REFERENCES songs(id),
    body            VARCHAR(2000) NOT NULL,
    moment_sec      INT CHECK (moment_sec >= 0),          -- optional "2:14" timestamp
    year_of_memory  SMALLINT CHECK (year_of_memory BETWEEN 1900 AND 2100),
    status          VARCHAR(16) NOT NULL DEFAULT 'VISIBLE', -- VISIBLE | HIDDEN | REMOVED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_stories_song_created ON stories (song_id, created_at DESC);
CREATE INDEX idx_stories_user_created ON stories (user_id, created_at DESC);

-- "I felt this too"
CREATE TABLE resonances (
    user_id     BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    story_id    BIGINT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, story_id)
);
CREATE INDEX idx_resonances_story ON resonances (story_id);

CREATE TABLE replies (
    id          BIGSERIAL PRIMARY KEY,
    story_id    BIGINT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    body        VARCHAR(1000) NOT NULL,
    status      VARCHAR(16) NOT NULL DEFAULT 'VISIBLE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_replies_story_created ON replies (story_id, created_at);

-- ---------------------------------------------------------------
-- Safety (required for App Store UGC review)
-- ---------------------------------------------------------------
CREATE TABLE reports (
    id           BIGSERIAL PRIMARY KEY,
    reporter_id  BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type  VARCHAR(16) NOT NULL,  -- STORY | REPLY | USER
    target_id    BIGINT      NOT NULL,
    reason       VARCHAR(32) NOT NULL,
    details      VARCHAR(500),
    resolved_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_reports_open ON reports (created_at) WHERE resolved_at IS NULL;

CREATE TABLE blocks (
    blocker_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (blocker_id, blocked_id),
    CHECK (blocker_id <> blocked_id)
);
