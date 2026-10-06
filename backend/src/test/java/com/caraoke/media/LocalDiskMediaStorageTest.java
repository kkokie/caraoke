package com.caraoke.media;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDiskMediaStorageTest {

    @TempDir Path dir;

    @Test
    void putResolveDelete() throws Exception {
        LocalDiskMediaStorage storage = new LocalDiskMediaStorage(dir.toString());

        storage.put("avatars/1/a.png", new byte[] {1, 2, 3}, "image/png");

        Path file = storage.resolve("avatars/1/a.png").orElseThrow();
        assertThat(Files.readAllBytes(file)).containsExactly(1, 2, 3);
        assertThat(storage.publicUrl("avatars/1/a.png")).isEqualTo("/media/avatars/1/a.png");

        storage.delete("avatars/1/a.png");
        storage.delete("avatars/1/a.png");                     // deleting twice is fine
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void keysCantEscapeTheMediaFolder() {
        LocalDiskMediaStorage storage = new LocalDiskMediaStorage(dir.toString());

        assertThat(storage.resolve("../../etc/passwd")).isEmpty();
        assertThat(storage.resolve("avatars/../../outside.png")).isEmpty();
        assertThat(storage.resolve("")).isEmpty();
        assertThatThrownBy(() -> storage.put("../evil.png", new byte[] {1}, "image/png"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
