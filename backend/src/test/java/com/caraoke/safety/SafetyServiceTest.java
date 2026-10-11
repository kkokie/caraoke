package com.caraoke.safety;

import com.caraoke.report.ReportReason;
import com.caraoke.report.ReportService;
import com.caraoke.report.ReportTarget;
import com.caraoke.safety.SafetyDtos.Kind;
import com.caraoke.safety.SafetyDtos.ReportRequest;
import com.caraoke.story.StoryDtos.StoryView;
import com.caraoke.story.StoryService;
import com.caraoke.user.UserDtos.Author;
import com.caraoke.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SafetyServiceTest {

    @Mock UserService users;
    @Mock StoryService stories;
    @Mock ReportService reports;
    SafetyService service;

    @BeforeEach
    void setUp() {
        service = new SafetyService(users, stories, reports);
    }

    @Test
    void reportSomeoneElsesStory() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(5L));
        when(stories.get("uid-ian", 20L)).thenReturn(story(20L, false));

        service.report("uid-ian", new ReportRequest(Kind.STORY, 20L, null, ReportReason.SPAM, " buy followers "));

        verify(reports).file(5L, ReportTarget.STORY, 20L, ReportReason.SPAM, " buy followers ");
    }

    @Test
    void cantReportYourOwnStoryOrYourself() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(5L));
        when(stories.get("uid-ian", 20L)).thenReturn(story(20L, true));
        when(users.findUserIdByHandle("ian")).thenReturn(Optional.of(5L));

        assertStatus(() -> service.report("uid-ian", new ReportRequest(Kind.STORY, 20L, null, ReportReason.OTHER, null)), 400);
        assertStatus(() -> service.report("uid-ian", new ReportRequest(Kind.USER, null, "ian", ReportReason.OTHER, null)), 400);
        verifyNoInteractions(reports);
    }

    @Test
    void reportAPerson() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(5L));
        when(users.findUserIdByHandle("spammer")).thenReturn(Optional.of(9L));

        service.report("uid-ian", new ReportRequest(Kind.USER, null, "spammer", ReportReason.IMPERSONATION, null));

        verify(reports).file(5L, ReportTarget.USER, 9L, ReportReason.IMPERSONATION, null);
    }

    @Test
    void needsAProfile() {
        when(users.findUserId("uid-new")).thenReturn(Optional.empty());
        assertStatus(() -> service.report("uid-new", new ReportRequest(Kind.USER, null, "x", ReportReason.SPAM, null)), 403);
    }

    private static StoryView story(long id, boolean mine) {
        return new StoryView(id, 7L, new Author("sam", "Sam", null), "words", null, null, null, "cream",
                Instant.parse("2026-10-11T00:00:00Z"), null, mine, 0, false);
    }

    private static void assertStatus(Runnable call, int status) {
        assertThatThrownBy(call::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(status));
    }
}
