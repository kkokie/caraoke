package com.caraoke.profile;

import com.caraoke.config.SecurityConfig;
import com.caraoke.profile.ProfileDtos.ProfileView;
import com.caraoke.story.StoryDtos.AuthorStats;
import com.caraoke.story.StoryDtos.StoryTile;
import com.caraoke.story.StoryDtos.StoryTilePage;
import com.caraoke.user.UserDtos.PublicProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.auth.dev-mode=true")
class ProfileControllerTest {

    private static final String USER = "X-Dev-User";

    @Autowired MockMvc mvc;
    @MockitoBean ProfileService service;

    @Test
    void profileRequiresAuth() throws Exception {
        mvc.perform(get("/api/profiles/sam")).andExpect(status().isUnauthorized());
    }

    @Test
    void profileHeader() throws Exception {
        PublicProfile sam = new PublicProfile("sam", "Sam", null, null, Instant.parse("2026-10-01T00:00:00Z"));
        when(service.profile("u1", "sam")).thenReturn(new ProfileView(sam, new AuthorStats(3, 12), false));

        mvc.perform(get("/api/profiles/sam").header(USER, "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.handle").value("sam"))
                .andExpect(jsonPath("$.stats.stories").value(3))
                .andExpect(jsonPath("$.stats.felt").value(12))
                .andExpect(jsonPath("$.me").value(false));
    }

    @Test
    void gridPassesCursor() throws Exception {
        when(service.stories("sam", 40L, 30)).thenReturn(new StoryTilePage(
                List.of(new StoryTile(30L, 7L, "Mr. Brightside", "The Killers", "https://art", 4)), 30L));

        mvc.perform(get("/api/profiles/sam/stories").param("before", "40").header(USER, "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].coverUrl").value("https://art"))
                .andExpect(jsonPath("$.nextCursor").value(30));
    }
}
