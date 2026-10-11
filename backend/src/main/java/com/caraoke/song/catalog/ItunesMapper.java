package com.caraoke.song.catalog;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Converts iTunes wire records into our provider-neutral CatalogTrack. */
final class ItunesMapper {

    // iTunes serves any size by rewriting this token in the artwork URL
    private static final String SMALL_ART = "100x100bb";
    private static final String LARGE_ART = "600x600bb";

    private ItunesMapper() { }

    static CatalogTrack toTrack(ItunesPayload.Track t) {
        return new CatalogTrack(
                String.valueOf(t.trackId()),
                t.trackName(),
                t.artistName(),
                t.collectionName(),
                upscaleArtwork(t.artworkUrl100()),
                toSeconds(t.trackTimeMillis()),
                toDate(t.releaseDate()),
                "Various Artists".equalsIgnoreCase(t.collectionArtistName()));
    }

    static LocalDate toDate(String iso) {
        if (iso == null || iso.length() < 10) return null;
        try {
            return LocalDate.parse(iso.substring(0, 10));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    static String upscaleArtwork(String url) {
        return url == null ? null : url.replace(SMALL_ART, LARGE_ART);
    }

    static Integer toSeconds(Long millis) {
        return millis == null ? null : (int) Math.round(millis / 1000.0);
    }
}
