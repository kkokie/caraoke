package com.caraoke.song.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Wire format of iTunes Search/Lookup responses. Only the fields we use are mapped. */
final class ItunesPayload {

    private ItunesPayload() { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Response(List<Track> results) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Track(
            String kind,
            Long trackId,
            String trackName,
            String artistName,
            String collectionName,
            String artworkUrl100,
            Long trackTimeMillis,
            String releaseDate,             // ISO instant, e.g. "2017-04-21T07:00:00Z"
            String collectionArtistName,    // "Various Artists" on compilations
            String previewUrl) {            // 30-second clip (stream only)

        boolean isSong() {
            return "song".equals(kind) && trackId != null && trackName != null && artistName != null;
        }
    }
}
