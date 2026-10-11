package com.caraoke.profile;

import com.caraoke.block.BlockService;
import com.caraoke.page.*;
import com.caraoke.profile.ProfilePageDtos.PageInput;
import com.caraoke.profile.ProfilePageDtos.PageView;
import com.caraoke.song.SongDtos.SongSummary;
import com.caraoke.song.SongService;
import com.caraoke.story.AuthorStoriesService;
import com.caraoke.story.StoryDtos.StoryTile;
import com.caraoke.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProfilePageServiceTest {

    private static final long SAM = 9L, IAN = 5L;

    private final UserService users = mock(UserService.class);
    private final PageService pages = mock(PageService.class);
    private final SongService songs = mock(SongService.class);
    private final AuthorStoriesService authorStories = mock(AuthorStoriesService.class);
    private final BlockService blocks = mock(BlockService.class);
    private final ProfilePageService service = new ProfilePageService(users, pages, songs, authorStories, blocks);

    private final PageSettings samsPage = new PageSettings(PageTheme.OCEAN, PageBackground.STARS, PageType.TYPEWRITER,
            null, 77L, List.of(PageBlock.PINNED_STORY, PageBlock.RAISED_ME), List.of(2L, 1L));

    @BeforeEach
    void setUp() {
        when(users.findUserIdByHandle("sam")).thenReturn(Optional.of(SAM));
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        when(pages.settings(SAM)).thenReturn(samsPage);
        when(songs.findSummaries(anyCollection())).thenReturn(Map.of(
                1L, new SongSummary(1L, "One", "A", null), 2L, new SongSummary(2L, "Two", "B", null)));
        when(authorStories.tilesFor(List.of(77L))).thenReturn(List.of(new StoryTile(77L, 1L, "One", "A", null, 4)));
    }

    @Test
    void assemblesThePageInTheOwnersOrder() {
        PageView page = service.page("uid-ian", "sam");

        assertThat(page.theme()).isEqualTo(PageTheme.OCEAN);
        assertThat(page.blocks()).containsExactly(PageBlock.PINNED_STORY, PageBlock.RAISED_ME);
        assertThat(page.pinnedStory().storyId()).isEqualTo(77L);
        assertThat(page.raisedMe()).extracting(SongSummary::id).containsExactly(2L, 1L);   // their order, not the map's
    }

    @Test
    void ifTheyBlockedYouThereIsNoPage() {
        when(blocks.hasBlocked(SAM, IAN)).thenReturn(true);
        assertThatThrownBy(() -> service.page("uid-ian", "sam")).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void ifYouBlockedThemTheirStoriesStayOffThePage() {
        when(blocks.eitherBlocked(IAN, SAM)).thenReturn(true);

        PageView page = service.page("uid-ian", "sam");

        assertThat(page.pinnedStory()).isNull();
        assertThat(page.lifeInSongs()).isEmpty();
        verify(authorStories, never()).lifeInSongs(anyLong());
    }

    @Test
    void youCanOnlyPinYourOwnStory() {
        when(authorStories.isVisibleStoryBy(IAN, 77L)).thenReturn(false);

        assertThatThrownBy(() -> service.save("uid-ian", new PageInput(null, null, null, null, 77L, null, null)))
                .isInstanceOf(ResponseStatusException.class);
        verify(pages, never()).save(anyLong(), any());
    }

    @Test
    void unknownSongsAreRefused() {
        when(songs.findSummaries(Set.of(99L))).thenReturn(Map.of());

        assertThatThrownBy(() -> service.setPageSong("uid-ian", 99L)).isInstanceOf(ResponseStatusException.class);
        verify(pages, never()).setPageSong(anyLong(), any());
    }
}
