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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the REAL server (Tomcat + full security chain + Postgres) and calls it over HTTP.
 * MockMvc tests can't catch bugs in Spring's internal /error forwarding; this can.
 * Regression: an authenticated request whose controller threw 404 used to come back as 401.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = "app.auth.dev-mode=true")
@Testcontainers
class HttpErrorsTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired TestRestTemplate http;

    @Test
    void signedInUserWithoutProfileGets404NotA401() {
        ResponseEntity<String> res = get("/api/me", "uid-brand-new");

        assertThat(res.getStatusCode().value()).isEqualTo(404);
        assertThat(res.getBody()).contains("Profile not created yet");   // the app routes to onboarding on this
    }

    @Test
    void missingCredentialsAreStillA401() {
        assertThat(http.getForEntity("/api/me", String.class).getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void validationErrorsKeepTheirStatus() {
        HttpHeaders headers = devUser("uid-someone");
        headers.add("Content-Type", "application/json");
        ResponseEntity<String> res = http.exchange("/api/me", HttpMethod.POST,
                new HttpEntity<>("{\"handle\":\"admin\",\"displayName\":\"A\"}", headers), String.class);

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(res.getBody()).contains("reserved");
    }

    private ResponseEntity<String> get(String path, String devUid) {
        return http.exchange(path, HttpMethod.GET, new HttpEntity<>(devUser(devUid)), String.class);
    }

    private static HttpHeaders devUser(String uid) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Dev-User", uid);
        return headers;
    }
}
