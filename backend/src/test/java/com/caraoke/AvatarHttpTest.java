package com.caraoke;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** Full round trip over real HTTP: create profile -> upload photo -> fetch it -> remove it. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "app.auth.dev-mode=true")
@Testcontainers
class AvatarHttpTest {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @TempDir static Path mediaDir;

    @DynamicPropertySource
    static void mediaDir(DynamicPropertyRegistry registry) {
        registry.add("app.media.local-dir", () -> mediaDir.toString());
    }

    @Autowired TestRestTemplate http;
    @Autowired ObjectMapper json;

    @Test
    void uploadServeAndRemoveAProfilePhoto() throws Exception {
        HttpHeaders me = new HttpHeaders();
        me.add("X-Dev-User", "uid-photo");
        me.setContentType(MediaType.APPLICATION_JSON);
        http.exchange("/api/me", HttpMethod.POST, new HttpEntity<>("{\"handle\":\"photogenic\",\"displayName\":\"P\"}", me), String.class);

        ResponseEntity<String> uploaded = http.exchange("/api/me/avatar", HttpMethod.PUT, multipart(PNG), String.class);
        assertThat(uploaded.getStatusCode().value()).isEqualTo(200);
        String url = json.readTree(uploaded.getBody()).get("avatarUrl").asText();
        assertThat(url).startsWith("/media/avatars/").endsWith(".png");

        ResponseEntity<byte[]> photo = http.getForEntity(url, byte[].class);       // public, no auth header
        assertThat(photo.getStatusCode().value()).isEqualTo(200);
        assertThat(photo.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
        assertThat(photo.getBody()).isEqualTo(PNG);

        ResponseEntity<String> notAnImage = http.exchange("/api/me/avatar", HttpMethod.PUT, multipart("nope".getBytes()), String.class);
        assertThat(notAnImage.getStatusCode().value()).isEqualTo(400);

        HttpHeaders auth = new HttpHeaders();
        auth.add("X-Dev-User", "uid-photo");
        JsonNode removed = json.readTree(http.exchange("/api/me/avatar", HttpMethod.DELETE, new HttpEntity<>(auth), String.class).getBody());
        assertThat(removed.get("avatarUrl").isNull()).isTrue();
        assertThat(http.getForEntity(url, byte[].class).getStatusCode().value()).isEqualTo(404);   // file cleaned up
    }

    private static HttpEntity<MultiValueMap<String, Object>> multipart(byte[] bytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Dev-User", "uid-photo");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(bytes) {
            @Override public String getFilename() { return "me.png"; }
        });
        return new HttpEntity<>(body, headers);
    }
}
