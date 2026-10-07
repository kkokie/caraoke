package com.caraoke.resonance;

import com.caraoke.common.ApiErrors;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Opaque keyset cursor for "stories I felt": the (createdAt, storyId) of the last row on a page.
 * createdAt alone isn't unique, so storyId breaks ties. Wire format: "2026-10-05T20:31:07.123456Z_42".
 */
public record FeltCursor(Instant createdAt, long storyId) {

    static FeltCursor of(Resonance r) {
        return new FeltCursor(r.getCreatedAt(), r.getId().getStoryId());
    }

    public String encode() {
        return createdAt + "_" + storyId;
    }

    /** @return null for a first page; 400 for anything malformed */
    public static FeltCursor decode(String raw) {
        if (raw == null || raw.isBlank()) return null;
        int split = raw.lastIndexOf('_');
        if (split <= 0) throw ApiErrors.badRequest("Bad cursor.");
        try {
            return new FeltCursor(Instant.parse(raw.substring(0, split)), Long.parseLong(raw.substring(split + 1)));
        } catch (DateTimeParseException | NumberFormatException e) {
            throw ApiErrors.badRequest("Bad cursor.");
        }
    }
}
