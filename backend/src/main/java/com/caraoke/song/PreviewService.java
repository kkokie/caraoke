package com.caraoke.song;

import com.caraoke.common.ApiErrors;
import com.caraoke.song.catalog.CatalogClient;
import com.caraoke.song.catalog.CatalogTrack;
import com.caraoke.song.catalog.CatalogUnavailableException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Apple's 30-second preview for a song. Apple's terms: previews are streamed, never downloaded,
 * saved or cached, and shown next to a link to the store with "courtesy of" attribution. So we
 * never store the audio or even the URL in the database; we look it up when a song page opens and
 * keep only the URL in memory for a few hours (the iTunes API allows ~20 lookups a minute).
 */
@Service
public class PreviewService {

    static final Duration REMEMBER_FOR = Duration.ofHours(6);

    private final SongRepository songs;
    private final CatalogClient catalog;
    private final Clock clock;
    private final Map<Long, Remembered> urls = new ConcurrentHashMap<>();

    public PreviewService(SongRepository songs, CatalogClient catalog, Clock clock) {
        this.songs = songs;
        this.catalog = catalog;
        this.clock = clock;
    }

    public record Preview(String url, String attribution) { }

    private record Remembered(String url, Instant until) { }

    /** Empty when Apple has no preview for this song (or the catalog is down: the page works without it). */
    public Optional<Preview> preview(long songId) {
        Remembered hit = urls.get(songId);
        if (hit != null && hit.until().isAfter(clock.instant())) return toPreview(hit.url());

        Song song = songs.findById(songId).orElseThrow(() -> ApiErrors.notFound("Song not found"));
        String url = lookup(song.getAppleId());
        urls.put(songId, new Remembered(url, clock.instant().plus(REMEMBER_FOR)));
        return toPreview(url);
    }

    private String lookup(String appleId) {
        if (appleId == null) return null;
        try {
            return catalog.lookup(appleId).map(CatalogTrack::previewUrl).orElse(null);
        } catch (CatalogUnavailableException e) {
            return null;
        }
    }

    private static Optional<Preview> toPreview(String url) {
        return Optional.ofNullable(url).map(u -> new Preview(u, "Preview courtesy of Apple Music"));
    }
}
