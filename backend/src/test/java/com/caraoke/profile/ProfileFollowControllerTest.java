package com.caraoke.profile;

import com.caraoke.config.SecurityConfig;
import com.caraoke.follow.FollowService;
import com.caraoke.user.UserDtos.Author;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileFollowController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.auth.dev-mode=true")
class ProfileFollowControllerTest {

    private static final String USER = "X-Dev-User";

    @Autowired MockMvc mvc;
    @MockitoBean ProfileFollowService service;

    @Test
    void followRequiresAuth() throws Exception {
        mvc.perform(put("/api/profiles/sam/follow")).andExpect(status().isUnauthorized());
    }

    @Test
    void followAndUnfollow() throws Exception {
        when(service.follow("u1", "sam")).thenReturn(new FollowService.State(true, 41));
        when(service.unfollow("u1", "sam")).thenReturn(new FollowService.State(false, 40));

        mvc.perform(put("/api/profiles/sam/follow").header(USER, "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(true))
                .andExpect(jsonPath("$.followers").value(41));
        mvc.perform(delete("/api/profiles/sam/follow").header(USER, "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(false));
    }

    @Test
    void followersAndFollowingLists() throws Exception {
        when(service.followers("sam")).thenReturn(List.of(new Author("jo", "Jo", null)));
        when(service.following("sam")).thenReturn(List.of(new Author("ian", "Ian", null)));

        mvc.perform(get("/api/profiles/sam/followers").header(USER, "u1"))
                .andExpect(jsonPath("$[0].handle").value("jo"));
        mvc.perform(get("/api/profiles/sam/following").header(USER, "u1"))
                .andExpect(jsonPath("$[0].handle").value("ian"));
    }
}
