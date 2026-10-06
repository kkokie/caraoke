package com.caraoke.story;

import com.caraoke.config.SecurityConfig;
import com.caraoke.story.StoryDtos.PostStoryRequest;
import com.caraoke.story.StoryDtos.StoryPage;
import com.caraoke.story.StoryDtos.StoryView;
import com.caraoke.user.UserDtos.Author;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoryController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.auth.dev-mode=true")
class StoryControllerTest {

    private static final String USER = "X-Dev-User";

    private static final StoryView STORY = new StoryView(
            100L, 7L, new Author("ian", "Ian", null), "senior year road trip", 134, 2009,
            Instant.parse("2026-10-02T17:00:00Z"), true);

    @Autowired MockMvc mvc;
    @MockitoBean StoryService service;

    @Test
    void feedRequiresAuth() throws Exception {
        mvc.perform(get("/api/songs/7/stories")).andExpect(status().isUnauthorized());
    }

    @Test
    void postReturns201WithTheStory() throws Exception {
        when(service.post(eq("u1"), eq(7L), any(PostStoryRequest.class))).thenReturn(STORY);

        mvc.perform(post("/api/songs/7/stories").header(USER, "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"senior year road trip\",\"momentSec\":134,\"yearOfMemory\":2009}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.author.handle").value("ian"))
                .andExpect(jsonPath("$.mine").value(true));
    }

    @Test
    void postWithoutBodyFieldIs400() throws Exception {
        mvc.perform(post("/api/songs/7/stories").header(USER, "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"momentSec\":134}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void feedPassesCursorAndLimit() throws Exception {
        when(service.feed("u1", 7L, 42L, 10)).thenReturn(new StoryPage(List.of(STORY), null));

        mvc.perform(get("/api/songs/7/stories").param("before", "42").param("limit", "10").header(USER, "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].body").value("senior year road trip"))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/stories/100").header(USER, "u1"))
                .andExpect(status().isNoContent());
        verify(service).delete("u1", 100L);
    }
}
