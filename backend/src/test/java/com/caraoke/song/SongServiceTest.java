package com.caraoke.song;

import com.caraoke.song.SongDtos.SearchResult;
import com.caraoke.song.SongDtos.SongView;
import com.caraoke.song.catalog.CatalogClient;
import com.caraoke.song.catalog.CatalogTrack;
import com.caraoke.song.catalog.CatalogUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    private static final CatalogTrack BRIGHTSIDE =
            new CatalogTrack("1440744581", "Mr. Brightside", "The Killers", "Hot Fuss", "https://art/600x600bb.jpg", 223);

    @Mock SongRepository songs;
    @Mock CatalogClient catalog;
    SongService service;

    @BeforeEach
    void setUp() {
        service = new SongService(songs, catalog);
    }

    // ---- search -------------------------------------------------------------

    @Test
    void searchTrimsQueryAndMapsResults() {
        when(catalog.search("killers", SongService.SEARCH_LIMIT)).thenReturn(List.of(BRIGHTSIDE));

        List<SearchResult> results = service.search("  killers ");

        assertThat(results).extracting(SearchResult::appleId).containsExactly("1440744581");
    }

    @Test
    void searchRejectsBlankAndTooLong() {
        assertStatus(() -> service.search("   "), 400);
        assertStatus(() -> service.search(null), 400);
        assertStatus(() -> service.search("x".repeat(SongService.MAX_QUERY_LENGTH + 1)), 400);
        verifyNoInteractions(catalog);
    }

    @Test
    void searchMapsCatalogOutageTo502() {
        when(catalog.search(any(), anyInt())).thenThrow(new CatalogUnavailableException("down", null));

        assertStatus(() -> service.search("killers"), 502);
    }

    // ---- resolve ------------------------------------------------------------

    @Test
    void resolveReusesExistingSongWithoutCallingCatalog() {
        when(songs.findByAppleId("1440744581")).thenReturn(Optional.of(saved(BRIGHTSIDE, 7L)));

        SongView view = service.resolve("1440744581");

        assertThat(view.id()).isEqualTo(7L);
        verifyNoInteractions(catalog);
    }

    @Test
    void resolveImportsNewSongFromCatalog() {
        when(songs.findByAppleId("1440744581")).thenReturn(Optional.empty());
        when(catalog.lookup("1440744581")).thenReturn(Optional.of(BRIGHTSIDE));
        when(songs.saveAndFlush(any(Song.class))).thenAnswer(inv -> withId(inv.getArgument(0), 9L));

        SongView view = service.resolve("1440744581");

        assertThat(view.id()).isEqualTo(9L);
        assertThat(view.title()).isEqualTo("Mr. Brightside");
        assertThat(view.listen().appleMusic()).endsWith("/song/1440744581");
    }

    @Test
    void resolveUnknownSongIs404() {
        when(songs.findByAppleId("1")).thenReturn(Optional.empty());
        when(catalog.lookup("1")).thenReturn(Optional.empty());

        assertStatus(() -> service.resolve("1"), 404);
    }

    @Test
    void resolveRaceFallsBackToTheRowThatWon() {
        when(songs.findByAppleId("1440744581"))
                .thenReturn(Optional.empty())                          // first check: not there yet
                .thenReturn(Optional.of(saved(BRIGHTSIDE, 3L)));       // after losing the insert race
        when(catalog.lookup("1440744581")).thenReturn(Optional.of(BRIGHTSIDE));
        when(songs.saveAndFlush(any(Song.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThat(service.resolve("1440744581").id()).isEqualTo(3L);
    }

    // ---- get ----------------------------------------------------------------

    @Test
    void getMissingSongIs404() {
        when(songs.findById(42L)).thenReturn(Optional.empty());

        assertStatus(() -> service.get(42L), 404);
    }

    // ---- helpers ------------------------------------------------------------

    private static Song saved(CatalogTrack track, long id) {
        return withId(Song.from(track), id);
    }

    private static Song withId(Song song, long id) {
        ReflectionTestUtils.setField(song, "id", id);   // the DB normally assigns this
        return song;
    }

    private static void assertStatus(Runnable call, int status) {
        assertThatThrownBy(call::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(status));
    }
}
