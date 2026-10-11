package com.caraoke.story;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StoryContentGuardTest {

    private static final Instant NOW = Instant.parse("2026-10-11T17:00:00Z");
    private final StoryRepository stories = mock(StoryRepository.class);
    // "badword" stands in for a real blocked term
    private final StoryContentGuard guard =
            new StoryContentGuard(stories, Clock.fixed(NOW, ZoneOffset.UTC), Set.of("badword"));

    @Test
    void blockedTermsAreCaughtThroughCommonSwaps() {
        assertThat(guard.containsBlocked("you are a B4DW0RD")).isTrue();
        assertThat(guard.containsBlocked("badwords")).isFalse();          // whole words only
        assertThat(guard.containsBlocked(null)).isFalse();
    }

    @Test
    void copyPastedStoriesAreRefused() {
        when(stories.existsSameTextSince(eq("the same words"), eq(NOW.minus(StoryContentGuard.DUPLICATE_WINDOW)), eq(-1L)))
                .thenReturn(true);

        assertThatThrownBy(() -> guard.check("  The   same\nwords ", null, null)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void editingKeepsYourOwnWords() {
        when(stories.existsSameTextSince(anyString(), any(), eq(42L))).thenReturn(false);

        guard.check("my story", null, 42L);
        verify(stories).existsSameTextSince("my story", NOW.minus(StoryContentGuard.DUPLICATE_WINDOW), 42L);
    }

    @Test
    void shippedListLoads() {
        new StoryContentGuard(stories, Clock.systemUTC());   // the real file parses (comments only today)
    }
}
