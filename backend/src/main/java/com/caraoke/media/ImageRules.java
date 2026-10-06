package com.caraoke.media;

import com.caraoke.common.ApiErrors;

/**
 * Validates uploads by their actual bytes ("magic numbers"), never by the client's
 * Content-Type or file name, which are trivially faked.
 */
public final class ImageRules {

    public static final int MAX_BYTES = 5 * 1024 * 1024;   // 5 MB; the app resizes to ~512px first

    public enum ImageType {
        JPEG("image/jpeg", ".jpg"),
        PNG("image/png", ".png"),
        WEBP("image/webp", ".webp");

        private final String contentType;
        private final String extension;

        ImageType(String contentType, String extension) {
            this.contentType = contentType;
            this.extension = extension;
        }

        public String contentType() { return contentType; }
        public String extension() { return extension; }
    }

    private ImageRules() { }

    /** @return the detected type, or throws 400 for empty, oversized, or non-image uploads */
    public static ImageType validate(byte[] bytes) {
        if (bytes == null || bytes.length == 0) throw ApiErrors.badRequest("Choose a photo to upload.");
        if (bytes.length > MAX_BYTES) throw ApiErrors.badRequest("That photo is too big (max 5 MB).");
        ImageType type = detect(bytes);
        if (type == null) throw ApiErrors.badRequest("Photos must be JPEG, PNG, or WebP.");
        return type;
    }

    static ImageType detect(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return ImageType.JPEG;
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return ImageType.PNG;
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return ImageType.WEBP;
        return null;
    }
}
