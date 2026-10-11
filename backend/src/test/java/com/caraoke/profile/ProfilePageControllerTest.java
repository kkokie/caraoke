package com.caraoke.profile;

import com.caraoke.config.SecurityConfig;
import com.caraoke.page.PageBackground;
import com.caraoke.page.PageBlock;
import com.caraoke.page.PageTheme;
import com.caraoke.page.PageType;
import com.caraoke.profile.ProfilePageDtos.PageView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfilePageController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.auth.dev-mode=true")
class ProfilePageControllerTest {

    private static final String USER = "X-Dev-User";
    private static final PageView PAGE = new PageView(PageTheme.ROSE, PageBackground.DOTS, PageType.CLASSIC,
            List.of(PageBlock.PAGE_SONG), null, null, List.of(), List.of());

    @Autowired MockMvc mvc;
    @MockitoBean ProfilePageService service;

    @Test
    void readSomeonesPage() throws Exception {
        when(service.page("u1", "sam")).thenReturn(PAGE);

        mvc.perform(get("/api/profiles/sam/page").header(USER, "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("ROSE"))
                .andExpect(jsonPath("$.blocks[0]").value("PAGE_SONG"));
    }

    @Test
    void setPageSong() throws Exception {
        when(service.setPageSong("u1", 7L)).thenReturn(PAGE);

        mvc.perform(put("/api/me/page/song").header(USER, "u1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"songId\":7}"))
                .andExpect(status().isOk());
    }

    @Test
    void unknownThemeIs400() throws Exception {
        mvc.perform(put("/api/me/page").header(USER, "u1")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"theme\":\"NEON_HTML\"}"))
                .andExpect(status().isBadRequest());
    }
}
