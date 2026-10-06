package com.caraoke.media;

import com.caraoke.media.ImageRules.ImageType;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageRulesTest {

    static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0};
    static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 0};

    @Test
    void detectsRealImageBytes() {
        assertThat(ImageRules.validate(JPEG)).isEqualTo(ImageType.JPEG);
        assertThat(ImageRules.validate(PNG)).isEqualTo(ImageType.PNG);
        assertThat(ImageRules.validate(WEBP)).isEqualTo(ImageType.WEBP);
    }

    @Test
    void rejectsNonImagesEmptyAndOversized() {
        assertBadRequest("<html>not an image</html>".getBytes());
        assertBadRequest(new byte[0]);
        assertBadRequest(null);
        byte[] huge = new byte[ImageRules.MAX_BYTES + 1];
        System.arraycopy(PNG, 0, huge, 0, PNG.length);
        assertBadRequest(huge);
    }

    private static void assertBadRequest(byte[] bytes) {
        assertThatThrownBy(() -> ImageRules.validate(bytes))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(400));
    }
}
