package com.caraoke.moderation;

import com.caraoke.report.ReportReason;
import com.caraoke.story.StoryModerationService;
import com.caraoke.user.Standing;
import com.caraoke.user.UserStandingService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StrikeServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-11T19:00:00Z");

    private final StrikeRepository strikes = mock(StrikeRepository.class);
    private final UserStandingService standing = mock(UserStandingService.class);
    private final StoryModerationService stories = mock(StoryModerationService.class);
    private final StrikeService service = new StrikeService(strikes, standing, stories, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void firstStrikeWarns() {
        when(strikes.countByUserIdAndVoidedAtIsNull(9L)).thenReturn(1L);

        assertThat(service.strike(9L, 20L, ReportReason.SPAM, 1L)).isEqualTo(Standing.WARNED);
        verify(strikes).save(any(Strike.class));
        verify(standing).setStanding(9L, Standing.WARNED, null);
        verifyNoInteractions(stories);
    }

    @Test
    void secondStrikeSuspendsForAWeek() {
        when(strikes.countByUserIdAndVoidedAtIsNull(9L)).thenReturn(2L);

        assertThat(service.strike(9L, 20L, ReportReason.HARASSMENT, 1L)).isEqualTo(Standing.SUSPENDED);
        verify(standing).setStanding(9L, Standing.SUSPENDED, Instant.parse("2026-10-18T19:00:00Z"));
    }

    @Test
    void thirdStrikeBansAndTakesEverythingDown() {
        when(strikes.countByUserIdAndVoidedAtIsNull(9L)).thenReturn(3L);

        assertThat(service.strike(9L, null, ReportReason.HATE, 1L)).isEqualTo(Standing.BANNED);
        verify(standing).setStanding(9L, Standing.BANNED, null);
        verify(stories).removeAllBy(9L);
    }

    @Test
    void reinstateVoidsStrikes() {
        service.reinstate(9L);

        verify(strikes).voidAll(9L, NOW);
        verify(standing).setStanding(9L, Standing.GOOD, null);
    }
}
