package com.caraoke.story;

import com.caraoke.config.SecurityConfig;
import com.caraoke.story.DiscoverDtos.YearCount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoryDiscoveryController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.auth.dev-mode=true")
class StoryDiscoveryControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean StoryDiscoveryService service;

    @Test
    void discoverNeedsSignIn() throws Exception {
        mvc.perform(get("/api/discover/years")).andExpect(status().isUnauthorized());
    }

    @Test
    void digWithNothingLeftIs204() throws Exception {
        when(service.dig(eq("u1"), anyList())).thenReturn(Optional.empty());

        mvc.perform(get("/api/discover/dig").param("exclude", "1", "2").header("X-Dev-User", "u1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void yearsListsCounts() throws Exception {
        when(service.years()).thenReturn(List.of(new YearCount(2009, 4)));

        mvc.perform(get("/api/discover/years").header("X-Dev-User", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2009))
                .andExpect(jsonPath("$[0].stories").value(4));
    }
}
