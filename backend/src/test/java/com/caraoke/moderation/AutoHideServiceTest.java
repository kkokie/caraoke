package com.caraoke.moderation;

import com.caraoke.report.ReportTarget;
import com.caraoke.story.StoryModerationService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AutoHideServiceTest {

    private final StoryModerationService stories = mock(StoryModerationService.class);
    private final AutoHideService autoHide = new AutoHideService(stories, 3);

    @Test
    void hidesAStoryAtTheThreshold() {
        when(stories.hideForReview(20L)).thenReturn(true);

        assertThat(autoHide.onReported(ReportTarget.STORY, 20L, 2)).isFalse();
        assertThat(autoHide.onReported(ReportTarget.STORY, 20L, 3)).isTrue();
        verify(stories, times(1)).hideForReview(20L);
    }

    @Test
    void neverHidesAPerson() {
        assertThat(autoHide.onReported(ReportTarget.USER, 9L, 50)).isFalse();
        verifyNoInteractions(stories);
    }
}
