package com.caraoke.config;

import com.caraoke.auth.AccountGate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class StandingFilterTest {

    /** Refuses every write, allows reads (like a suspension). */
    private final AccountGate suspended = (uid, write) -> write ? Optional.of("Suspended") : Optional.empty();
    private final StandingFilter filter = new StandingFilter(suspended);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void writesAreRefusedReadsPass() throws Exception {
        signIn("uid-sam");
        assertThat(run("POST", "/api/songs/1/stories")).isEqualTo(403);
        assertThat(run("GET", "/api/songs/1/stories")).isEqualTo(200);
    }

    @Test
    void deletingYourAccountAndReadingYourStandingAlwaysWork() throws Exception {
        signIn("uid-sam");
        assertThat(run("DELETE", "/api/me")).isEqualTo(200);
        assertThat(run("GET", "/api/me/standing")).isEqualTo(200);
    }

    @Test
    void signedOutRequestsAreNotChecked() throws Exception {
        assertThat(run("POST", "/api/reports")).isEqualTo(200);
    }

    private int run(String method, String path) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest(method, path);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        return res.getStatus();
    }

    private static void signIn(String uid) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(uid, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
    }
}
