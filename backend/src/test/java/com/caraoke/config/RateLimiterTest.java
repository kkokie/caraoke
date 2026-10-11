package com.caraoke.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    /** A clock we can move. */
    static class MovableClock extends Clock {
        Instant now = Instant.parse("2026-10-11T17:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void allowsUpToTheLimitPerMinuteThenResets() {
        MovableClock clock = new MovableClock();
        RateLimiter limiter = new RateLimiter(clock);

        for (int i = 0; i < 3; i++) assertThat(limiter.allow("u:ian", 3)).isTrue();
        assertThat(limiter.allow("u:ian", 3)).isFalse();
        assertThat(limiter.allow("u:sam", 3)).isTrue();          // per caller

        clock.now = clock.now.plusSeconds(60);
        assertThat(limiter.allow("u:ian", 3)).isTrue();          // new minute
    }
}
