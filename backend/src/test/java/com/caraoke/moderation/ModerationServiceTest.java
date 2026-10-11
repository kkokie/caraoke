package com.caraoke.moderation;

import com.caraoke.moderation.ModerationDtos.Action;
import com.caraoke.moderation.ModerationDtos.Decision;
import com.caraoke.report.ReportReason;
import com.caraoke.report.ReportService;
import com.caraoke.report.ReportService.OpenReport;
import com.caraoke.report.ReportTarget;
import com.caraoke.report.Resolution;
import com.caraoke.story.StoryModerationService;
import com.caraoke.user.Standing;
import com.caraoke.user.UserDtos.AccountSummary;
import com.caraoke.user.UserStandingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ModerationServiceTest {

    private final UserStandingService users = mock(UserStandingService.class);
    private final StoryModerationService stories = mock(StoryModerationService.class);
    private final ReportService reports = mock(ReportService.class);
    private final StrikeService strikes = mock(StrikeService.class);
    private final ModerationQueueService queue = mock(ModerationQueueService.class);
    private final ModerationService service = new ModerationService(users, stories, reports, strikes, queue);

    private static final AccountSummary SAM = new AccountSummary(9L, "sam", "Sam", null, Standing.WARNED, null);

    @BeforeEach
    void admin() {
        when(users.requireAdmin("uid-ian")).thenReturn(1L);
        when(users.summaries(Set.of(9L))).thenReturn(Map.of(9L, SAM));
    }

    @Test
    void nonAdminsAreTurnedAway() {
        when(users.requireAdmin("uid-sam")).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> service.decideStory("uid-sam", 20L, new Decision(Action.STRIKE, null)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(stories, strikes);
    }

    @Test
    void dismissBringsAHiddenStoryBack() {
        service.decideStory("uid-ian", 20L, new Decision(Action.DISMISS, null));

        verify(stories).restore(20L);
        verify(reports).resolve(ReportTarget.STORY, 20L, Resolution.DISMISSED, 1L);
        verifyNoInteractions(strikes);
    }

    @Test
    void strikeRemovesTheStoryAndStrikesTheAuthorForTheFirstReason() {
        when(reports.openReports(ReportTarget.STORY, 20L))
                .thenReturn(List.of(new OpenReport(ReportReason.HATE, null, Instant.now())));
        when(stories.remove(20L)).thenReturn(9L);

        service.decideStory("uid-ian", 20L, new Decision(Action.STRIKE, null));

        verify(strikes).strike(9L, 20L, ReportReason.HATE, 1L);
        verify(reports).resolve(ReportTarget.STORY, 20L, Resolution.ACTIONED, 1L);
    }

    @Test
    void banAPersonWithAGivenReason() {
        when(users.findForModeration("sam")).thenReturn(Optional.of(SAM));

        service.decidePerson("uid-ian", "sam", new Decision(Action.BAN, ReportReason.IMPERSONATION));

        verify(strikes).ban(9L, null, ReportReason.IMPERSONATION, 1L);
        verify(reports).resolve(ReportTarget.USER, 9L, Resolution.ACTIONED, 1L);
    }

    @Test
    void moderatorsCantStrikeThemselves() {
        when(users.findForModeration("ian"))
                .thenReturn(Optional.of(new AccountSummary(1L, "ian", "Ian", null, Standing.GOOD, null)));

        assertThatThrownBy(() -> service.decidePerson("uid-ian", "ian", new Decision(Action.BAN, null)))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(strikes);
    }
}
