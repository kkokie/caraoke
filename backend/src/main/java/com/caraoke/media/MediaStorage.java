package com.caraoke.media;

/**
 * Where uploaded media (profile photos, later story photos) lives.
 * Dev: LocalDiskMediaStorage (files on disk, served by this API at /media/**).
 * Prod: an S3-compatible implementation (Cloudflare R2) selected by app.media.storage.
 * The database only ever stores keys like "avatars/42/9f1c….jpg".
 */
public interface MediaStorage {

    void put(String key, byte[] bytes, String contentType);

    /** Best-effort: missing keys are fine. */
    void delete(String key);

    /**
     * Where a client can fetch the object. May be relative ("/media/…") in dev,
     * which the app resolves against the API host; absolute (CDN) in prod.
     */
    String publicUrl(String key);
}
