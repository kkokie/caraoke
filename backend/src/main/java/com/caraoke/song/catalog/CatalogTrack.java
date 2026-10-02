package com.caraoke.song.catalog;

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
        Integer durationSec) { }
