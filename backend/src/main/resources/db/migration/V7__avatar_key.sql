-- V7: profile photos. We store the object-storage KEY, never a URL (URLs depend on where
-- media is served from: local disk in dev, Cloudflare R2/S3 + CDN in prod).
-- avatar_url was never written by the app, so a rename is safe.
ALTER TABLE users RENAME COLUMN avatar_url TO avatar_key;
ALTER TABLE users ALTER COLUMN avatar_key TYPE VARCHAR(255);
