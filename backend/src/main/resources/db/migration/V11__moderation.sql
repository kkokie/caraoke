-- V11: moderation. Who can moderate, where each account stands, strikes, and how reports were resolved.

-- Admins are set by hand (no endpoint grants it): UPDATE users SET role = 'ADMIN' WHERE handle = '...';
ALTER TABLE users ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT 'MEMBER';
ALTER TABLE users ADD CONSTRAINT users_role CHECK (role IN ('MEMBER', 'ADMIN'));

-- GOOD -> WARNED -> SUSPENDED (until suspended_until) -> BANNED. Derived from active strikes.
ALTER TABLE users ADD COLUMN standing VARCHAR(16) NOT NULL DEFAULT 'GOOD';
ALTER TABLE users ADD CONSTRAINT users_standing CHECK (standing IN ('GOOD', 'WARNED', 'SUSPENDED', 'BANNED'));
ALTER TABLE users ADD COLUMN suspended_until TIMESTAMPTZ;

-- A banned person who deletes their account can't sign straight back up with the same sign-in.
CREATE TABLE banned_auth_uids (
    auth_uid   VARCHAR(128) PRIMARY KEY,
    banned_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- One strike per upheld report. Voided strikes (a moderator reinstated the account) don't count.
CREATE TABLE strikes (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    story_id    BIGINT,                                    -- no FK: the story may be deleted later
    reason      VARCHAR(32) NOT NULL,
    issued_by   BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    voided_at   TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_strikes_user_active ON strikes (user_id) WHERE voided_at IS NULL;

-- How a report was closed, and by whom.
ALTER TABLE reports ADD COLUMN resolution VARCHAR(16);
ALTER TABLE reports ADD COLUMN resolved_by BIGINT REFERENCES users(id) ON DELETE SET NULL;
CREATE INDEX idx_reports_open_target ON reports (target_type, target_id) WHERE resolved_at IS NULL;
