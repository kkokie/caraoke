package com.songstories.user;

import com.songstories.config.SecurityConfig;
import com.songstories.user.UserDtos.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.auth.dev-mode=true")
class UserControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean UserService service;

    private static final PublicProfile IAN =
            new PublicProfile("ian", "Ian", "music nerd", null, Instant.parse("2026-09-28T00:00:00Z"));

    @Test
    void meRequiresAuth() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void meUsesCallerUid() throws Exception {
        when(service.getMe("uid-123")).thenReturn(IAN);

        mvc.perform(get("/api/me").header("X-Dev-User", "uid-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").value("ian"))
                .andExpect(jsonPath("$.authUid").doesNotExist());   // never leak the auth id
    }

    @Test
    void createProfileValidatesBody() throws Exception {
        mvc.perform(post("/api/me").header("X-Dev-User", "uid-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"\",\"displayName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProfileReturns201() throws Exception {
        when(service.createProfile(eq("uid-123"), any())).thenReturn(IAN);

        mvc.perform(post("/api/me").header("X-Dev-User", "uid-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"handle\":\"ian\",\"displayName\":\"Ian\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.handle").value("ian"));
    }

    @Test
    void publicProfileIsOpen() throws Exception {
        when(service.getByHandle("ian")).thenReturn(IAN);

        mvc.perform(get("/api/users/ian")).andExpect(status().isOk());
    }

    @Test
    void deleteAccount() throws Exception {
        mvc.perform(delete("/api/me").header("X-Dev-User", "uid-123"))
                .andExpect(status().isNoContent());
        verify(service).deleteAccount("uid-123");
    }
}
