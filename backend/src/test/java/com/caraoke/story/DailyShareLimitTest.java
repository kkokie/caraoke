package com.caraoke.story;

import com.caraoke.story.StoryDtos.ShareQuota;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DailyShareLimitTest {

    private static final Instant NOW = Instant.parse("2026-10-07T20:00:00Z");
    private static final long IAN = 5L;

    private final StoryRepository stories = mock(StoryRepository.class);
    private final DailyShareLimit limit = new DailyShareLimit(stories, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void firstStoryEverIsAllowed() {
        when(stories.findFirstByUserIdOrderByCreatedAtDesc(IAN)).thenReturn(Optional.empty());

        assertThat(limit.status(IAN)).isEqualTo(new ShareQuota(true, null));
        limit.check(IAN);                                         // no exception
    }

    @Test
    void within24HoursYouWaitAndLearnWhen() {
        lastStoryAt(NOW.minusSeconds(6 * 3600));                  // shared 6h ago

        assertThat(limit.status(IAN)).isEqualTo(new ShareQuota(false, NOW.plusSeconds(18 * 3600)));
        assertThatThrownBy(() -> limit.check(IAN))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(429));
    }

    @Test
    void after24HoursYouCanShareAgain() {
        lastStoryAt(NOW.minusSeconds(24 * 3600));                 // exactly a day ago

        assertThat(limit.status(IAN).canShare()).isTrue();
        limit.check(IAN);
    }

    private void lastStoryAt(Instant at) {
        Story last = new Story(IAN, 1L, "yesterday's story", null, null);
        ReflectionTestUtils.setField(last, "createdAt", at);
        when(stories.findFirstByUserIdOrderByCreatedAtDesc(IAN)).thenReturn(Optional.of(last));
    }
}
