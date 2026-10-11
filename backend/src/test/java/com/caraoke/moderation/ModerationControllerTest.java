package com.caraoke.moderation;

import com.caraoke.config.SecurityConfig;
import com.caraoke.moderation.ModerationDtos.*;
import com.caraoke.report.ReportReason;
import com.caraoke.user.Standing;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModerationController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.auth.dev-mode=true")
class ModerationControllerTest {

    private static final String USER = "X-Dev-User";

    @Autowired MockMvc mvc;
    @MockitoBean ModerationService service;

    @Test
    void queueNeedsSignIn() throws Exception {
        mvc.perform(get("/api/admin/reports")).andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminsGet403() throws Exception {
        when(service.queue("uid-sam", null, 0)).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Moderators only."));
        mvc.perform(get("/api/admin/reports").header(USER, "uid-sam")).andExpect(status().isForbidden());
    }

    @Test
    void queuePage() throws Exception {
        Person sam = new Person("sam", "Sam", Standing.GOOD, null, 0);
        QueueItem item = new QueueItem(Kind.USER, 41L, 2, List.of(ReportReason.SPAM), null, sam, null, List.of());
        when(service.queue("uid-ian", 40L, 10)).thenReturn(new QueuePage(List.of(item), null));

        mvc.perform(get("/api/admin/reports?after=40&size=10").header(USER, "uid-ian"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].person.handle").value("sam"))
                .andExpect(jsonPath("$.items[0].reporters").value(2))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());
    }

    @Test
    void decideAStory() throws Exception {
        Person sam = new Person("sam", "Sam", Standing.WARNED, null, 1);
        when(service.decideStory("uid-ian", 20L, new Decision(Action.STRIKE, null))).thenReturn(new Outcome(sam, 3));

        mvc.perform(post("/api/admin/stories/20/decision").header(USER, "uid-ian")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"STRIKE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person.standing").value("WARNED"))
                .andExpect(jsonPath("$.reportsClosed").value(3));
    }

    @Test
    void decisionNeedsAnAction() throws Exception {
        mvc.perform(post("/api/admin/users/sam/decision").header(USER, "uid-ian")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
