package com.caraoke.media;

import org.springframework.stereotype.Component;

/** Dev only: no scanning service yet, so every valid image passes. Replace before launch. */
@Component
class LocalImageScreening implements ImageScreening {

    @Override
    public void check(byte[] bytes, String contentType) {
        // Intentionally empty until a real scanner is wired in (see ImageScreening)
    }
}
