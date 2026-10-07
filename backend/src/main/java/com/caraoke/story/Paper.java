package com.caraoke.story;

import com.caraoke.common.ApiErrors;

import java.util.Locale;

/** The paper a story card is printed on. Plus papers come with caraoke+. */
public enum Paper {
    CREAM(false), DUSK(false), SAGE(false), INK(false),
    ROSE(true), TAPE(true);

    private final boolean plus;

    Paper(boolean plus) {
        this.plus = plus;
    }

    public boolean isPlus() {
        return plus;
    }

    /** API value: lowercase ("cream"). */
    public String apiValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Null means "not chosen"; unknown names are a 400. */
    static Paper parse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return valueOf(raw.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ApiErrors.badRequest("Pick one of the papers.");
        }
    }
}
