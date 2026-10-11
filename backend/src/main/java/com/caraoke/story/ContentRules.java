package com.caraoke.story;

import com.caraoke.common.ApiErrors;

import java.util.regex.Pattern;

/**
 * Cheap checks every story passes before it's saved: no links (the #1 spam vector, and caraoke is
 * about the music, not traffic) and no keyboard-mash filler. Deeper checks (blocked terms,
 * duplicates) live in StoryContentGuard since they need data.
 */
final class ContentRules {

    // http(s)://…, www.…, or a bare domain like spam.com / bit.ly/x
    private static final Pattern LINK = Pattern.compile(
            "(?i)(https?://|www\\.|\\b[a-z0-9-]{2,}\\.(com|net|org|io|ly|gg|me|co|app|xyz|link|info|biz|ru|tk|shop|site|online)\\b)");
    // the same character 15+ times in a row ("aaaaaaaaaaaaaaa", "!!!!!!!!!!!!!!!")
    private static final Pattern MASH = Pattern.compile("(.)\\1{14,}");

    private ContentRules() { }

    static void check(String text) {
        if (text == null) return;
        if (LINK.matcher(text).find()) throw ApiErrors.badRequest("Links aren’t allowed in stories. Keep it about the song.");
        if (MASH.matcher(text).find()) throw ApiErrors.badRequest("That looks like keyboard mash. Tell it in words.");
    }
}
