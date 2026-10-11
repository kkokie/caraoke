package com.caraoke;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/** The real security chain: suspended and banned accounts are turned away before any controller. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "app.auth.dev-mode=true")
@Testcontainers
class StandingHttpTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    /** A write that reaches a controller cheaply (no one has this handle). */
    private static final String REPORT_NOBODY = "{\"kind\":\"USER\",\"handle\":\"nobody_here\",\"reason\":\"SPAM\"}";

    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;

    @Test
    void suspendedCanReadButNotWrite() {
        user("suspended1", "SUSPENDED", "now() + interval '7 days'");

        assertThat(call(HttpMethod.GET, "/api/me", "uid-suspended1").getStatusCode().value()).isEqualTo(200);
        ResponseEntity<String> write = call(HttpMethod.POST, "/api/reports", "uid-suspended1", REPORT_NOBODY);
        assertThat(write.getStatusCode().value()).isEqualTo(403);
        assertThat(write.getBody()).contains("ACCOUNT_STANDING").contains("suspended until");
    }

    @Test
    void anEndedSuspensionLetsYouWriteAgain() {
        user("suspended2", "SUSPENDED", "now() - interval '1 minute'");

        // Gets past the gate to the controller, which 404s on the unknown handle
        assertThat(call(HttpMethod.POST, "/api/reports", "uid-suspended2", REPORT_NOBODY).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void bannedCanOnlySeeTheirStandingAndDeleteTheAccount() {
        user("banned1", "BANNED", "NULL");

        assertThat(call(HttpMethod.GET, "/api/search/people?q=ab", "uid-banned1").getStatusCode().value()).isEqualTo(403);
        ResponseEntity<String> standing = call(HttpMethod.GET, "/api/me/standing", "uid-banned1");
        assertThat(standing.getStatusCode().value()).isEqualTo(200);
        assertThat(standing.getBody()).contains("BANNED");
        assertThat(call(HttpMethod.DELETE, "/api/me", "uid-banned1").getStatusCode().value()).isEqualTo(204);
    }

    @Test
    void bannedProfilesAreGoneFromPublicView() {
        user("banned2", "BANNED", "NULL");

        assertThat(http.getForEntity("/api/users/banned2", String.class).getStatusCode().value()).isEqualTo(404);
    }

    private void user(String handle, String standing, String suspendedUntilSql) {
        jdbc.update("INSERT INTO users (auth_uid, handle, display_name, standing, suspended_until) VALUES (?, ?, ?, ?, "
                + suspendedUntilSql + ")", "uid-" + handle, handle, handle, standing);
    }

    private ResponseEntity<String> call(HttpMethod method, String path, String uid) {
        return call(method, path, uid, null);
    }

    private ResponseEntity<String> call(HttpMethod method, String path, String uid, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Dev-User", uid);
        if (json != null) headers.add("Content-Type", "application/json");
        return http.exchange(path, method, new HttpEntity<>(json, headers), String.class);
    }
}
