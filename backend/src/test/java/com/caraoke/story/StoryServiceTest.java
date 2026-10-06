package com.caraoke.story;

import com.caraoke.resonance.ResonanceService;
import com.caraoke.resonance.ResonanceSummary;
import com.caraoke.song.ListenLinks;
import com.caraoke.song.SongDtos.SongView;
import com.caraoke.song.SongService;
import com.caraoke.story.StoryDtos.PostStoryRequest;
import com.caraoke.story.StoryDtos.StoryPage;
import com.caraoke.story.StoryDtos.StoryView;
import com.caraoke.user.UserDtos.Author;
import com.caraoke.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    private static final long IAN = 5L;
    private static final long OTHER = 6L;
    private static final long SONG_ID = 7L;

    private static final SongView SONG = new SongView(
            SONG_ID, "Mr. Brightside", "The Killers", "Hot Fuss", null, 223,
            new ListenLinks(null, "https://spotify", "https://youtube"));

    private static final Map<Long, Author> AUTHORS = Map.of(
            IAN, new Author("ian", "Ian", null),
            OTHER, new Author("sam", "Sam", null));

    @Mock StoryRepository stories;
    @Mock SongService songs;
    @Mock UserService users;
    @Mock ResonanceService resonances;
    StoryService service;

    @BeforeEach
    void setUp() {
        service = new StoryService(stories, songs, users, new StoryViewAssembler(users, resonances));
    }

    // ---- post ---------------------------------------------------------------

    @Test
    void postSavesCleanedStoryAndReturnsItAsMine() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        when(songs.get(SONG_ID)).thenReturn(SONG);
        when(stories.save(any(Story.class))).thenAnswer(inv -> withId(inv.getArgument(0), 100L));
        when(users.findAuthors(anySet())).thenReturn(AUTHORS);
        when(resonances.summaries(anyList(), any())).thenReturn(Map.of());

        StoryView view = service.post("uid-ian", SONG_ID, new PostStoryRequest("  senior year road trip  ", 134, 2009));

        ArgumentCaptor<Story> saved = ArgumentCaptor.forClass(Story.class);
        verify(stories).save(saved.capture());
        assertThat(saved.getValue().getBody()).isEqualTo("senior year road trip");
        assertThat(saved.getValue().getMomentSec()).isEqualTo(134);
        assertThat(saved.getValue().getYearOfMemory()).isEqualTo((short) 2009);

        assertThat(view.id()).isEqualTo(100L);
        assertThat(view.mine()).isTrue();
        assertThat(view.author().handle()).isEqualTo("ian");
        assertThat(view.yearOfMemory()).isEqualTo(2009);
    }

    @Test
    void postWithoutProfileIs403() {
        when(users.findUserId("uid-new")).thenReturn(Optional.empty());

        assertStatus(() -> service.post("uid-new", SONG_ID, new PostStoryRequest("hi", null, null)), 403);
        verifyNoInteractions(stories);
    }

    @Test
    void postMomentPastTheEndIs400() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        when(songs.get(SONG_ID)).thenReturn(SONG);

        assertStatus(() -> service.post("uid-ian", SONG_ID, new PostStoryRequest("hi", 999, null)), 400);
        verifyNoInteractions(stories);
    }

    // ---- feed ---------------------------------------------------------------

    @Test
    void feedReturnsOnePageAndACursorWhenMoreExist() {
        when(songs.get(SONG_ID)).thenReturn(SONG);
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        // page size 2 -> repository asked for 3; the 3rd row only proves there's another page
        when(stories.findBySongIdAndStatusAndIdLessThanOrderByIdDesc(
                eq(SONG_ID), eq(StoryStatus.VISIBLE), eq(Long.MAX_VALUE), any(Limit.class)))
                .thenReturn(List.of(story(30L, IAN), story(20L, OTHER), story(10L, OTHER)));
        when(users.findAuthors(anySet())).thenReturn(AUTHORS);
        when(resonances.summaries(List.of(30L, 20L), IAN)).thenReturn(Map.of(20L, new ResonanceSummary(3, true)));

        StoryPage page = service.feed("uid-ian", SONG_ID, null, 2);

        assertThat(page.items()).extracting(StoryView::id).containsExactly(30L, 20L);
        assertThat(page.items()).extracting(StoryView::mine).containsExactly(true, false);
        assertThat(page.items()).extracting(StoryView::resonanceCount).containsExactly(0L, 3L);   // no row -> 0
        assertThat(page.items()).extracting(StoryView::resonatedByMe).containsExactly(false, true);
        assertThat(page.nextCursor()).isEqualTo(20L);
    }

    @Test
    void feedLastPageHasNoCursor() {
        when(songs.get(SONG_ID)).thenReturn(SONG);
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        when(stories.findBySongIdAndStatusAndIdLessThanOrderByIdDesc(
                eq(SONG_ID), eq(StoryStatus.VISIBLE), eq(20L), any(Limit.class)))
                .thenReturn(List.of(story(10L, OTHER)));
        when(users.findAuthors(anySet())).thenReturn(AUTHORS);
        when(resonances.summaries(anyList(), any())).thenReturn(Map.of());

        StoryPage page = service.feed("uid-ian", SONG_ID, 20L, 2);

        assertThat(page.items()).extracting(StoryView::id).containsExactly(10L);
        assertThat(page.nextCursor()).isNull();
    }

    // ---- delete -------------------------------------------------------------

    @Test
    void deleteOwnStory() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        Story mine = story(30L, IAN);
        when(stories.findById(30L)).thenReturn(Optional.of(mine));

        service.delete("uid-ian", 30L);

        verify(stories).delete(mine);
    }

    @Test
    void deleteSomeoneElsesStoryIs403() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        when(stories.findById(20L)).thenReturn(Optional.of(story(20L, OTHER)));

        assertStatus(() -> service.delete("uid-ian", 20L), 403);
        verify(stories, never()).delete(any(Story.class));
    }

    @Test
    void deleteMissingStoryIs404() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        when(stories.findById(anyLong())).thenReturn(Optional.empty());

        assertStatus(() -> service.delete("uid-ian", 1L), 404);
    }

    // ---- helpers ------------------------------------------------------------

    private static Story story(long id, long authorId) {
        return withId(new Story(authorId, SONG_ID, "a memory", null, null), id);
    }

    private static Story withId(Story story, long id) {
        ReflectionTestUtils.setField(story, "id", id);   // the DB normally assigns this
        return story;
    }

    private static void assertStatus(Runnable call, int status) {
        assertThatThrownBy(call::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(status));
    }
}
