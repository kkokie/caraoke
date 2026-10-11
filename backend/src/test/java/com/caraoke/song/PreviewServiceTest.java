package com.caraoke.song;

import com.caraoke.song.catalog.CatalogClient;
import com.caraoke.song.catalog.CatalogTrack;
import com.caraoke.song.catalog.CatalogUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PreviewServiceTest {

    private final SongRepository songs = mock(SongRepository.class);
    private final CatalogClient catalog = mock(CatalogClient.class);
    private final PreviewService service =
            new PreviewService(songs, catalog, Clock.fixed(Instant.parse("2026-10-11T17:00:00Z"), ZoneOffset.UTC));

    @Test
    void looksUpOnceThenRemembersTheUrl() {
        whenSong(7L, "1440744581");
        when(catalog.lookup("1440744581")).thenReturn(Optional.of(track("https://audio-ssl.itunes.apple.com/clip.m4a")));

        assertThat(service.preview(7L)).get().extracting(PreviewService.Preview::url).isEqualTo("https://audio-ssl.itunes.apple.com/clip.m4a");
        assertThat(service.preview(7L)).isPresent();
        verify(catalog, times(1)).lookup("1440744581");
    }

    @Test
    void noPreviewOrCatalogDownMeansNone() {
        whenSong(8L, "1");
        when(catalog.lookup("1")).thenThrow(new CatalogUnavailableException("down", null));
        assertThat(service.preview(8L)).isEmpty();

        whenSong(9L, "2");
        when(catalog.lookup("2")).thenReturn(Optional.of(track(null)));
        assertThat(service.preview(9L)).isEmpty();
    }

    private void whenSong(long id, String appleId) {
        Song song = Song.from(new CatalogTrack(appleId, "T", "A", null, null, 200));
        ReflectionTestUtils.setField(song, "id", id);
        when(songs.findById(id)).thenReturn(Optional.of(song));
    }

    private static CatalogTrack track(String previewUrl) {
        return new CatalogTrack("x", "T", "A", null, null, 200, null, false, previewUrl);
    }
}
