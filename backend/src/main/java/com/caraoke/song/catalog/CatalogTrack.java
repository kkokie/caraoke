package com.caraoke.song.catalog;

import java.time.LocalDate;

/**
 * A track as the outside music catalog describes it, independent of any provider.
 * appleId is a String on purpose: IDs are opaque, and JS clients lose precision on big numbers.
 */
public record CatalogTrack(
        String appleId,
        String title,
        String artist,
        String album,
        String artworkUrl,
        Integer durationSec,
        LocalDate releaseDate,      // null when unknown; originals usually predate covers
        boolean compilation,        // "Various Artists" collections (karaoke packs, hits albums)
        String previewUrl) {        // Apple's 30s clip: stream only, never store or download

    /** For callers that only know the basics (tests, lookups). */
    public CatalogTrack(String appleId, String title, String artist, String album, String artworkUrl, Integer durationSec) {
        this(appleId, title, artist, album, artworkUrl, durationSec, null, false, null);
    }

    public CatalogTrack(String appleId, String title, String artist, String album, String artworkUrl, Integer durationSec,
                        LocalDate releaseDate, boolean compilation) {
        this(appleId, title, artist, album, artworkUrl, durationSec, releaseDate, compilation, null);
    }
}
