package com.caraoke.story;

import com.caraoke.story.StoryDtos.ShareQuota;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * One story a day (a rolling 24 hours since your last one), so people share what really
 * matters to them instead of posting volume. Edits don't count.
 */
@Component
class DailyShareLimit {

    static final Duration WINDOW = Duration.ofHours(24);

    private final StoryRepository stories;
    private final Clock clock;

    DailyShareLimit(StoryRepository stories, Clock clock) {
        this.stories = stories;
        this.clock = clock;
    }

    ShareQuota status(long userId) {
        Instant next = nextShareAt(userId);
        return new ShareQuota(next == null, next);
    }

    /** 429 with a friendly message when today's story is already shared. */
    void check(long userId) {
        if (nextShareAt(userId) != null) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "You’ve shared your story for today. Come back tomorrow for the next one.");
        }
    }

    /** Null when you can share now; otherwise when your next story unlocks. */
    private Instant nextShareAt(long userId) {
        return stories.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(last -> last.getCreatedAt().plus(WINDOW))
                .filter(unlock -> unlock.isAfter(clock.instant()))
                .orElse(null);
    }
}
