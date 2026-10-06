package com.caraoke.song;

import com.caraoke.song.catalog.CatalogTrack;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Request/response shapes for the song API. */
public final class SongDtos {

    private SongDtos() { }

    /** A catalog hit. Not in our DB yet; becomes a Song when someone opens it. */
    public record SearchResult(
            String appleId,
            String title,
            String artist,
            String album,
            String artworkUrl,
            Integer durationSec) {

        static SearchResult of(CatalogTrack t) {
            return new SearchResult(t.appleId(), t.title(), t.artist(), t.album(), t.artworkUrl(), t.durationSec());
        }
    }

    /** Just enough to draw a song on a tile or a row. */
    public record SongSummary(long id, String title, String artist, String artworkUrl) {

        static SongSummary of(Song s) {
            return new SongSummary(s.getId(), s.getTitle(), s.getArtist(), s.getAlbumArtUrl());
        }
    }

    public record ResolveRequest(
            @NotBlank @Pattern(regexp = "\\d{1,20}", message = "appleId must be numeric") String appleId) { }

    /** A song page. `id` is OUR id; it's what stories will reference. */
    public record SongView(
            long id,
            String title,
            String artist,
            String album,
            String artworkUrl,
            Integer durationSec,
            ListenLinks listen) {

        static SongView of(Song s) {
            return new SongView(
                    s.getId(), s.getTitle(), s.getArtist(), s.getAlbum(), s.getAlbumArtUrl(), s.getDurationSec(),
                    ListenLinks.forSong(s.getAppleId(), s.getTitle(), s.getArtist()));
        }
    }
}
