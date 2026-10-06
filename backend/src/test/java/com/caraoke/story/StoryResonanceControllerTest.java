package com.caraoke.story;

import com.caraoke.config.SecurityConfig;
import com.caraoke.resonance.ResonanceSummary;
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

@WebMvcTest(StoryResonanceController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.auth.dev-mode=true")
class StoryResonanceControllerTest {

    private static final String USER = "X-Dev-User";

    @Autowired MockMvc mvc;
    @MockitoBean StoryResonanceService service;

    @Test
    void resonateRequiresAuth() throws Exception {
        mvc.perform(put("/api/stories/20/resonance")).andExpect(status().isUnauthorized());
    }

    @Test
    void resonateReturnsTheNewCount() throws Exception {
        when(service.resonate("u1", 20L)).thenReturn(new ResonanceSummary(4, true));

        mvc.perform(put("/api/stories/20/resonance").header(USER, "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(4))
                .andExpect(jsonPath("$.mine").value(true));
    }

    @Test
    void unresonateReturnsTheNewCount() throws Exception {
        when(service.unresonate("u1", 20L)).thenReturn(new ResonanceSummary(3, false));

        mvc.perform(delete("/api/stories/20/resonance").header(USER, "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mine").value(false));
    }

    @Test
    void resonatorsListsAuthors() throws Exception {
        when(service.resonators(20L)).thenReturn(List.of(new Author("jo", "Jo", null)));

        mvc.perform(get("/api/stories/20/resonators").header(USER, "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].handle").value("jo"));
    }
}
