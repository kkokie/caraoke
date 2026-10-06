package com.caraoke.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Dev storage: files under app.media.local-dir (default ./data/media, git-ignored),
 * served back by LocalMediaController at /media/**. Zero setup, no cloud account needed.
 */
@Component
@ConditionalOnProperty(name = "app.media.storage", havingValue = "local", matchIfMissing = true)
public class LocalDiskMediaStorage implements MediaStorage {

    static final String URL_PREFIX = "/media/";

    private final Path root;

    public LocalDiskMediaStorage(@Value("${app.media.local-dir:./data/media}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
    }

    @Override
    public void put(String key, byte[] bytes, String contentType) {
        Path file = resolve(key).orElseThrow(() -> new IllegalArgumentException("Bad media key: " + key));
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't store " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        resolve(key).ifPresent(file -> {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // best-effort cleanup
            }
        });
    }

    @Override
    public String publicUrl(String key) {
        return URL_PREFIX + key;
    }

    /** The file for a key, or empty if the key would escape the media folder ("../../etc/passwd"). */
    Optional<Path> resolve(String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        Path file = root.resolve(key).normalize();
        return file.startsWith(root) && !file.equals(root) ? Optional.of(file) : Optional.empty();
    }
}
