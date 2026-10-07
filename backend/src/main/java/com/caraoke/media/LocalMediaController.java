package com.caraoke.media;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.time.Duration;

/** Serves locally stored media in dev. In prod, media comes from the CDN and this isn't loaded. */
@RestController
@ConditionalOnProperty(name = "app.media.storage", havingValue = "local", matchIfMissing = true)
public class LocalMediaController {

    private final LocalDiskMediaStorage storage;

    public LocalMediaController(LocalDiskMediaStorage storage) {
        this.storage = storage;
    }

    @GetMapping("/media/**")
    public ResponseEntity<Resource> get(HttpServletRequest request) {
        String key = request.getRequestURI().substring(LocalDiskMediaStorage.URL_PREFIX.length());
        return storage.resolve(key)
                .filter(Files::isRegularFile)
                .map(file -> {
                    Resource resource = new FileSystemResource(file);
                    MediaType type = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
                    // Keys are unique per upload, so a file never changes: cache it hard
                    return ResponseEntity.ok()
                            .contentType(type)
                            .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
