package com.caraoke.story;

import com.caraoke.resonance.ResonanceService;
import com.caraoke.song.SongDtos.SongSummary;
import com.caraoke.song.SongService;
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
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoryDiscoveryServiceTest {

    @Mock StoryRepository stories;
    @Mock SongService songs;
    @Mock UserService users;
    @Mock ResonanceService resonances;
    StoryDiscoveryService service;

    @BeforeEach
    void setUp() {
        service = new StoryDiscoveryService(stories, new StoryViewAssembler(users, resonances), songs, users);
    }

    @Test
    void digReturnsTheStoryWithItsSong() {
        Story found = new Story(6L, 7L, "x".repeat(100), 134, (short) 2009);
        ReflectionTestUtils.setField(found, "id", 40L);
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(5L));
        when(stories.findRandomForDig(5L, StoryDiscoveryService.MIN_DIG_CHARS, "{1,2}")).thenReturn(Optional.of(found));
        when(users.findAuthors(anySet())).thenReturn(Map.of(6L, new Author("sam", "Sam", null)));
        when(resonances.summaries(anyList(), any())).thenReturn(Map.of());
        when(songs.findSummaries(anyCollection())).thenReturn(Map.of(7L, new SongSummary(7L, "Summer Static", "The Low Tides", null)));

        var result = service.dig("uid-ian", List.of(1L, 2L)).orElseThrow();

        assertThat(result.story().id()).isEqualTo(40L);
        assertThat(result.song().title()).isEqualTo("Summer Static");
    }

    @Test
    void signedOutViewersDigWithAnIdNoOneHas() {
        when(users.findUserId("uid-anon")).thenReturn(Optional.empty());
        when(stories.findRandomForDig(-1L, StoryDiscoveryService.MIN_DIG_CHARS, "{}")).thenReturn(Optional.empty());

        assertThat(service.dig("uid-anon", List.of())).isEmpty();
    }

    @Test
    void excludeListIsCapped() {
        List<Long> many = LongStream.rangeClosed(1, 500).boxed().toList();
        String literal = StoryDiscoveryService.arrayLiteral(many);
        assertThat(literal.split(",")).hasSize(StoryDiscoveryService.MAX_EXCLUDE);
        assertThat(StoryDiscoveryService.arrayLiteral(null)).isEqualTo("{}");
    }

    @Test
    void nonsenseYearsAreA400() {
        assertThatThrownBy(() -> service.byYear("uid-ian", 99999, null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    void songsFullOfStoriesSkipSongsThatNoLongerExist() {
        StoryRepository.SongCount a = count(7L, 12), b = count(8L, 3);
        when(stories.topSongs(StoryDiscoveryService.TOP_SONGS)).thenReturn(List.of(a, b));
        when(songs.findSummaries(anyCollection())).thenReturn(Map.of(7L, new SongSummary(7L, "Summer Static", "The Low Tides", null)));

        var top = service.songsFullOfStories();

        assertThat(top).hasSize(1);
        assertThat(top.get(0).stories()).isEqualTo(12);
    }

    private static StoryRepository.SongCount count(long songId, long n) {
        return new StoryRepository.SongCount() {
            public long getSongId() { return songId; }
            public long getStories() { return n; }
        };
    }
}
