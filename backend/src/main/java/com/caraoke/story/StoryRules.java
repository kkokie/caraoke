package com.caraoke.story;

import com.caraoke.common.ApiErrors;

import java.time.Year;

/**
 * Story input rules in one place (like HandleRules), so they're easy to test and to mirror in the app.
 * Each method returns the cleaned value or throws a 400 with a user-facing message.
 */
final class StoryRules {

    static final int MAX_BODY = 2000;
    static final int MIN_YEAR = 1900;

    private StoryRules() { }

    static String body(String raw) {
        String body = raw == null ? "" : raw.strip();
        if (body.isEmpty()) throw ApiErrors.badRequest("Write a few words about this song.");
        if (body.length() > MAX_BODY) throw ApiErrors.badRequest("Stories are up to " + MAX_BODY + " characters.");
        return body;
    }

    /** Moment must land inside the song when we know its length. */
    static Integer moment(Integer momentSec, Integer songDurationSec) {
        if (momentSec == null) return null;
        if (momentSec < 0) throw ApiErrors.badRequest("That moment isn't in the song.");
        if (songDurationSec != null && momentSec > songDurationSec) {
            throw ApiErrors.badRequest("That moment is past the end of the song.");
        }
        return momentSec;
    }

    /** Memories can't come from the future. */
    static Short year(Integer year, Year currentYear) {
        if (year == null) return null;
        if (year < MIN_YEAR || year > currentYear.getValue()) {
            throw ApiErrors.badRequest("Pick a year between " + MIN_YEAR + " and " + currentYear.getValue() + ".");
        }
        return year.shortValue();
    }
}
