package com.caraoke.moderation;

import com.caraoke.user.Standing;

import java.time.Duration;

/** Remove → warn → suspend → ban: what N active strikes add up to. Pure, so it's easy to test and tune. */
final class StrikeLadder {

    static final Duration SUSPENSION = Duration.ofDays(7);

    private StrikeLadder() { }

    static Standing standingFor(long activeStrikes) {
        if (activeStrikes <= 0) return Standing.GOOD;
        if (activeStrikes == 1) return Standing.WARNED;
        if (activeStrikes == 2) return Standing.SUSPENDED;
        return Standing.BANNED;
    }
}
