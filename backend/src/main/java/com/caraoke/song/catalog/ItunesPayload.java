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
            Long trackTimeMillis) {

        boolean isSong() {
            return "song".equals(kind) && trackId != null && trackName != null && artistName != null;
        }
    }
}
