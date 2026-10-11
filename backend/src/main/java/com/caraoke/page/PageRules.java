package com.caraoke.page;

import com.caraoke.common.ApiErrors;

import java.util.LinkedHashSet;
import java.util.List;

/** What a saved page may contain. Pure, so the rules are easy to test. */
final class PageRules {

    static final int MAX_RAISED_SONGS = 5;

    private PageRules() { }

    /** Shown blocks, in order, each once. An empty list is fine (a bare page). */
    static List<PageBlock> blocks(List<PageBlock> blocks) {
        if (blocks == null) return Page.DEFAULT_BLOCKS;
        return List.copyOf(new LinkedHashSet<>(blocks));
    }

    static List<Long> raisedSongs(List<Long> songIds) {
        if (songIds == null) return List.of();
        List<Long> distinct = List.copyOf(new LinkedHashSet<>(songIds));
        if (distinct.size() > MAX_RAISED_SONGS) {
            throw ApiErrors.badRequest("Pick up to " + MAX_RAISED_SONGS + " songs that raised you.");
        }
        return distinct;
    }

    static <T> T orDefault(T value, T fallback) {
        return value == null ? fallback : value;
    }
}
