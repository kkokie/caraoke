package com.caraoke.song;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * "Listen on…" deep links. We never host audio; we send people to where they already listen.
 * Apple has an exact link; Spotify and YouTube Music get a search until we store their IDs.
 */
public record ListenLinks(String appleMusic, String spotify, String youtubeMusic) {

    static ListenLinks forSong(String appleId, String title, String artist) {
        String query = encode(title + " " + artist);
        return new ListenLinks(
                appleId == null ? null : "https://music.apple.com/us/song/" + appleId,
                "https://open.spotify.com/search/" + query,
                "https://music.youtube.com/search?q=" + query);
    }

    private static String encode(String s) {
        // URLEncoder does form encoding (space -> '+'); paths want %20
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
