package com.caraoke.song;

import com.caraoke.song.catalog.CatalogTrack;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SearchRankerTest {

    // Shaped like what iTunes returns for "mask off": soundalikes first, the original buried
    private static final List<CatalogTrack> MASK_OFF = List.of(
            track("1", "Mask Off", "Ganja White Night", "Mask Off - Single", "2019-03-01", false),
            track("2", "Mask Off", "Elior Norlander", "Mask Off - Single", "2021-06-11", false),
            track("3", "Mask Off (Karaoke Version)", "Karaoke Hits Band", "Karaoke Hits 2017", "2017-06-01", true),
            track("4", "Mask Off (Remix) [feat. Kendrick Lamar]", "Future", "Mask Off (Remix)", "2017-06-30", false),
            track("5", "Mask Off", "Future", "FUTURE", "2017-02-17", false),
            track("6", "Mask Off", "Future", "Mask Off - Single", "2017-02-17", false));

    @Test
    void theOriginalBeatsSoundalikesRemixesAndKaraoke() {
        List<CatalogTrack> ranked = SearchRanker.rank("mask off", MASK_OFF, Set.of(), 25);

        assertThat(ranked.get(0).artist()).isEqualTo("Future");
        assertThat(ranked.get(0).title()).isEqualTo("Mask Off");
        assertThat(ranked.get(ranked.size() - 1).title()).contains("Karaoke");
    }

    @Test
    void theOriginalWinsEvenWhenItunesPutsItLast() {
        List<CatalogTrack> buried = new java.util.ArrayList<>();
        buried.add(track("gwn", "Mask Off", "Ganja White Night", "Mask Off - Single", "2019-03-01", false));
        for (int i = 0; i < 48; i++) buried.add(track("x" + i, "Unrelated " + i, "Someone " + i, "Z", "2020-01-01", false));
        buried.add(track("future", "Mask Off", "Future", "FUTURE", "2017-02-17", false));

        assertThat(SearchRanker.rank("mask off", buried, Set.of(), 25).get(0).appleId()).isEqualTo("future");
    }

    @Test
    void duplicateCopiesOfTheSameSongCollapse() {
        List<CatalogTrack> ranked = SearchRanker.rank("mask off", MASK_OFF, Set.of(), 25);

        assertThat(ranked.stream().filter(t -> t.artist().equals("Future") && t.title().equals("Mask Off"))).hasSize(1);
        assertThat(ranked).hasSize(5);
    }

    @Test
    void namingTheArtistPullsTheirVersionUp() {
        List<CatalogTrack> ranked = SearchRanker.rank("mask off elior norlander", MASK_OFF, Set.of(), 25);

        assertThat(ranked.get(0).artist()).isEqualTo("Elior Norlander");
    }

    @Test
    void songsPeopleOpenedHereGetANudgeAndTheLimitHolds() {
        List<CatalogTrack> tracks = List.of(
                track("a", "Ocean", "Artist A", "X", null, false),
                track("b", "Ocean", "Artist B", "Y", null, false));

        assertThat(SearchRanker.rank("ocean", tracks, Set.of("b"), 25).get(0).appleId()).isEqualTo("b");
        assertThat(SearchRanker.rank("ocean", tracks, Set.of(), 1)).hasSize(1);
    }

    @Test
    void titleDecorationIsIgnoredWhenMatching() {
        assertThat(SearchRanker.baseTitle("Mask Off (feat. Kendrick Lamar) - Radio Edit")).isEqualTo("mask off");
        assertThat(SearchRanker.baseTitle("Don't Stop Me Now [Remastered 2011]")).isEqualTo("don't stop me now");
    }

    private static CatalogTrack track(String id, String title, String artist, String album, String released, boolean compilation) {
        return new CatalogTrack(id, title, artist, album, null, 200,
                released == null ? null : LocalDate.parse(released), compilation);
    }
}
