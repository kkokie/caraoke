package com.caraoke.user;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Handle rules live in one place so the API, the DB constraint and the app agree:
 * 3-20 chars, lowercase letters / digits / underscore, not reserved.
 */
public final class HandleRules {

    private static final Pattern FORMAT = Pattern.compile("^[a-z0-9_]{3,20}$");

    // Handles that could be used to impersonate the app or staff
    private static final Set<String> RESERVED = Set.of(
            "admin", "administrator", "support", "help", "moderator", "mod",
            "staff", "official", "caraoke", "root", "system", "api", "me");

    private HandleRules() { }

    public static String normalize(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT).replaceFirst("^@", "");
    }

    /** @return null if valid, otherwise a user-facing reason */
    public static String validate(String normalized) {
        if (!FORMAT.matcher(normalized).matches()) {
            return "Handles are 3-20 characters: letters, numbers, and underscores.";
        }
        if (RESERVED.contains(normalized)) {
            return "That handle is reserved.";
        }
        return null;
    }
}
