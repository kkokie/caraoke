package com.caraoke.story;

import com.caraoke.resonance.ResonanceService;
import com.caraoke.resonance.ResonanceSummary;
import com.caraoke.user.UserDtos.Author;
import com.caraoke.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryResonanceServiceTest {

    private static final long IAN = 5L;
    private static final long SAM = 6L;

    @Mock StoryRepository stories;
    @Mock ResonanceService resonances;
    @Mock UserService users;
    StoryResonanceService service;

    @BeforeEach
    void setUp() {
        service = new StoryResonanceService(stories, resonances, users);
    }

    @Test
    void resonateWithSomeoneElsesStory() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        when(stories.findById(20L)).thenReturn(Optional.of(story(20L, SAM)));
        when(resonances.add(IAN, 20L)).thenReturn(new ResonanceSummary(1, true));

        assertThat(service.resonate("uid-ian", 20L)).isEqualTo(new ResonanceSummary(1, true));
    }

    @Test
    void cantResonateWithYourOwnStory() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        when(stories.findById(30L)).thenReturn(Optional.of(story(30L, IAN)));

        assertStatus(() -> service.resonate("uid-ian", 30L), 400);
        verifyNoInteractions(resonances);
    }

    @Test
    void hiddenStoriesAreNotFound() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        Story hidden = story(20L, SAM);
        ReflectionTestUtils.setField(hidden, "status", StoryStatus.HIDDEN);
        when(stories.findById(20L)).thenReturn(Optional.of(hidden));

        assertStatus(() -> service.resonate("uid-ian", 20L), 404);
    }

    @Test
    void needsAProfile() {
        when(users.findUserId("uid-new")).thenReturn(Optional.empty());

        assertStatus(() -> service.resonate("uid-new", 20L), 403);
        verifyNoInteractions(stories, resonances);
    }

    @Test
    void unresonateNeverChecksTheStory() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        when(resonances.remove(IAN, 20L)).thenReturn(new ResonanceSummary(0, false));

        assertThat(service.unresonate("uid-ian", 20L).mine()).isFalse();
        verifyNoInteractions(stories);
    }

    @Test
    void resonatorsKeepNewestFirstOrder() {
        when(stories.findById(20L)).thenReturn(Optional.of(story(20L, SAM)));
        when(resonances.recentResonatorIds(20L, StoryResonanceService.RESONATOR_LIMIT)).thenReturn(List.of(7L, IAN));
        when(users.findAuthors(List.of(7L, IAN))).thenReturn(Map.of(
                IAN, new Author("ian", "Ian", null),
                7L, new Author("jo", "Jo", null)));

        assertThat(service.resonators(20L)).extracting(Author::handle).containsExactly("jo", "ian");
    }

    @Test
    void resonatorsOfMissingStoryIs404() {
        when(stories.findById(anyLong())).thenReturn(Optional.empty());

        assertStatus(() -> service.resonators(1L), 404);
    }

    private static Story story(long id, long authorId) {
        Story s = new Story(authorId, 7L, "a memory", null, null);
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }

    private static void assertStatus(Runnable call, int status) {
        assertThatThrownBy(call::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(status));
    }
}
