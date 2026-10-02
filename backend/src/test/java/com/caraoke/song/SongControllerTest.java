package com.caraoke.song;

import com.caraoke.common.ApiErrors;
import com.caraoke.config.SecurityConfig;
import com.caraoke.song.SongDtos.SearchResult;
import com.caraoke.song.SongDtos.SongView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SongController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.auth.dev-mode=true")
class SongControllerTest {

    private static final String USER = "X-Dev-User";

    @Autowired MockMvc mvc;
    @MockitoBean SongService service;

    private static final SongView BRIGHTSIDE = new SongView(
            7L, "Mr. Brightside", "The Killers", "Hot Fuss", "https://art", 223,
            ListenLinks.forSong("1440744581", "Mr. Brightside", "The Killers"));

    @Test
    void searchRequiresAuth() throws Exception {
        mvc.perform(get("/api/songs/search").param("q", "killers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchReturnsCatalogHits() throws Exception {
        when(service.search("killers")).thenReturn(List.of(
                new SearchResult("1440744581", "Mr. Brightside", "The Killers", "Hot Fuss", "https://art", 223)));

        mvc.perform(get("/api/songs/search").param("q", "killers").header(USER, "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appleId").value("1440744581"))
                .andExpect(jsonPath("$[0].title").value("Mr. Brightside"));
    }

    @Test
    void resolveRejectsNonNumericAppleId() throws Exception {
        mvc.perform(post("/api/songs/resolve").header(USER, "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appleId\":\"abc\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void resolveReturnsOurSong() throws Exception {
        when(service.resolve("1440744581")).thenReturn(BRIGHTSIDE);

        mvc.perform(post("/api/songs/resolve").header(USER, "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"appleId\":\"1440744581\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.listen.appleMusic").value("https://music.apple.com/us/song/1440744581"))
                .andExpect(jsonPath("$.listen.spotify").value("https://open.spotify.com/search/Mr.%20Brightside%20The%20Killers"));
    }

    @Test
    void getUnknownSongIs404() throws Exception {
        when(service.get(99L)).thenThrow(ApiErrors.notFound("Song not found"));

        mvc.perform(get("/api/songs/99").header(USER, "u1"))
                .andExpect(status().isNotFound());
    }
}
