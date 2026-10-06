package com.caraoke.user;

import com.caraoke.config.SecurityConfig;
import com.caraoke.user.UserDtos.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
    @MockitoBean AvatarService avatars;

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

    @Test
    void avatarUploadIsMultipartPut() throws Exception {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        when(avatars.set(eq("uid-123"), any(byte[].class))).thenReturn(
                new PublicProfile("ian", "Ian", null, "/media/avatars/1/x.png", Instant.parse("2026-09-28T00:00:00Z")));

        mvc.perform(multipart("/api/me/avatar")
                        .file(new MockMultipartFile("file", "me.png", "image/png", png))
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .header("X-Dev-User", "uid-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("/media/avatars/1/x.png"));
    }
}
