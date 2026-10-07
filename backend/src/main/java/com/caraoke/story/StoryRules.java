package com.caraoke.story;

import com.caraoke.common.ApiErrors;

import java.time.Year;

/**
 * Story input rules in one place (like HandleRules), so they're easy to test and to mirror in the app.
 * Each method returns the cleaned value or throws a 400 with a user-facing message.
 */
final class StoryRules {

    static final int MAX_BODY = 10_000;   // blog-length; mirrored by the stories_body_length DB check
    static final int MIN_YEAR = 1900;
    static final int MAX_LYRIC = 120;     // a line or two, never whole lyrics (copyright); DB column matches

    private StoryRules() { }

    static String body(String raw) {
        String body = raw == null ? "" : raw.strip();
        if (body.isEmpty()) throw ApiErrors.badRequest("Write a few words about this song.");
        if (body.length() > MAX_BODY) throw ApiErrors.badRequest("Stories are up to 10,000 characters.");
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

    /** Optional. Quotes the person typed are dropped (the app draws its own). Blank = none. */
    static String lyric(String raw) {
        if (raw == null) return null;
        String line = raw.strip().replaceAll("^[\"“”'‘’]+|[\"“”'‘’]+$", "").strip();
        if (line.isEmpty()) return null;
        if (line.length() > MAX_LYRIC) throw ApiErrors.badRequest("Keep the lyric to a line or two (120 characters).");
        return line;
    }

    /** Plus papers wait for caraoke+; until subscriptions exist nobody has them. */
    static Paper paper(String raw) {
        Paper paper = Paper.parse(raw);
        if (paper != null && paper.isPlus()) throw ApiErrors.forbidden("That paper comes with caraoke+.");
        return paper;
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
