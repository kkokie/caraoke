-- V10: record that each person agreed to the Terms + Community Guidelines (and which version),
-- and confirmed they're 13 or older, at sign-up. Existing dev accounts predate this: NULL.
ALTER TABLE users ADD COLUMN terms_version VARCHAR(16);
ALTER TABLE users ADD COLUMN terms_accepted_at TIMESTAMPTZ;
