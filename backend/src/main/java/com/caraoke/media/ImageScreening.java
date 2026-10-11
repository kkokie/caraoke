package com.caraoke.media;

/**
 * Checks a photo before anyone can see it. Throws 400 to refuse it.
 * Dev: LocalImageScreening allows everything. Before launch (Phase 3) this gets an automatic
 * scan (e.g. AWS Rekognition moderation labels) and CSAM hash matching with required reporting.
 */
public interface ImageScreening {

    void check(byte[] bytes, String contentType);
}
