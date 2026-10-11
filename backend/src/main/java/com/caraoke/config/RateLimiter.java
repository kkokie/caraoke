package com.caraoke.config;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed one-minute windows per caller. In memory, which is right for one API instance; move to
 * Redis (or the platform's limiter) if we ever run several.
 */
public class RateLimiter {

    private final Clock clock;
    private final Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private volatile long window = -1;

    public RateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** @return false when `key` has used up `limit` requests this minute. */
    public boolean allow(String key, int limit) {
        long now = clock.millis() / 60_000;
        if (now != window) {          // a new minute: forget the old one
            synchronized (this) {
                if (now != window) {
                    counts.clear();
                    window = now;
                }
            }
        }
        return counts.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet() <= limit;
    }
}
