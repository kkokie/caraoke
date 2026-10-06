package com.caraoke.story;

import com.caraoke.resonance.ResonanceService;
import com.caraoke.resonance.ResonanceSummary;
import com.caraoke.song.SongDtos.SongSummary;
import com.caraoke.song.SongService;
import com.caraoke.story.StoryDtos.AuthorStats;
import com.caraoke.story.StoryDtos.StoryTile;
import com.caraoke.story.StoryDtos.StoryTilePage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorStoriesServiceTest {

    private static final long IAN = 5L;

    @Mock StoryRepository stories;
    @Mock SongService songs;
    @Mock ResonanceService resonances;
    AuthorStoriesService service;

    @BeforeEach
    void setUp() {
        service = new AuthorStoriesService(stories, songs, resonances);
    }

    @Test
    void tilesUseAlbumArtAndCarryResonanceCounts() {
        when(stories.findByUserIdAndStatusAndIdLessThanOrderByIdDesc(
                eq(IAN), eq(StoryStatus.VISIBLE), eq(Long.MAX_VALUE), any(Limit.class)))
                .thenReturn(List.of(story(30L, 7L), story(20L, 8L), story(10L, 7L)));
        when(songs.findSummaries(anyCollection())).thenReturn(Map.of(
                7L, new SongSummary(7L, "Mr. Brightside", "The Killers", "https://art/7"),
                8L, new SongSummary(8L, "Yellow", "Coldplay", "https://art/8")));
        when(resonances.summaries(List.of(30L, 20L), null)).thenReturn(Map.of(30L, new ResonanceSummary(4, false)));

        StoryTilePage page = service.tiles(IAN, null, 2);

        assertThat(page.items()).extracting(StoryTile::storyId).containsExactly(30L, 20L);
        assertThat(page.items()).extracting(StoryTile::coverUrl).containsExactly("https://art/7", "https://art/8");
        assertThat(page.items()).extracting(StoryTile::resonanceCount).containsExactly(4L, 0L);
        assertThat(page.nextCursor()).isEqualTo(20L);
    }

    @Test
    void emptyProfileMakesNoSongOrResonanceQueries() {
        when(stories.findByUserIdAndStatusAndIdLessThanOrderByIdDesc(anyLong(), any(), anyLong(), any(Limit.class)))
                .thenReturn(List.of());

        StoryTilePage page = service.tiles(IAN, null, 30);

        assertThat(page.items()).isEmpty();
        assertThat(page.nextCursor()).isNull();
        verifyNoInteractions(songs, resonances);
    }

    @Test
    void statsCountStoriesAndTotalFelt() {
        when(stories.findIdsByUserIdAndStatus(IAN, StoryStatus.VISIBLE)).thenReturn(List.of(30L, 20L, 10L));
        when(resonances.totalFor(List.of(30L, 20L, 10L))).thenReturn(12L);

        assertThat(service.stats(IAN)).isEqualTo(new AuthorStats(3, 12));
    }

    private static Story story(long id, long songId) {
        Story s = new Story(IAN, songId, "a memory", null, null);
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }
}
