package com.caraoke.story;

import com.caraoke.common.ApiErrors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks that need data: blocked terms (moderation/blocked-terms.txt) and copy-pasted stories.
 * The same words shared recently by anyone are refused, which stops spam rings and story farming.
 */
@Component
class StoryContentGuard {

    static final Duration DUPLICATE_WINDOW = Duration.ofDays(30);

    private final StoryRepository stories;
    private final Clock clock;
    private final Set<String> blockedTerms;

    @org.springframework.beans.factory.annotation.Autowired
    StoryContentGuard(StoryRepository stories, Clock clock) {
        this(stories, clock, load("moderation/blocked-terms.txt"));
    }

    StoryContentGuard(StoryRepository stories, Clock clock, Set<String> blockedTerms) {
        this.stories = stories;
        this.clock = clock;
        this.blockedTerms = blockedTerms;
    }

    /** @param editingId the story being edited (it may keep its own words), or null for a new story */
    void check(String body, String lyric, Long editingId) {
        if (containsBlocked(body) || containsBlocked(lyric)) {
            throw ApiErrors.badRequest("Something in there goes against our community guidelines.");
        }
        String normalized = normalizeForDuplicates(body);
        if (stories.existsSameTextSince(normalized, clock.instant().minus(DUPLICATE_WINDOW), editingId == null ? -1 : editingId)) {
            throw ApiErrors.badRequest("These exact words were already shared. Tell it your own way.");
        }
    }

    boolean containsBlocked(String text) {
        if (text == null || blockedTerms.isEmpty()) return false;
        return Arrays.stream(unLeet(text).split("[^\\p{L}\\p{N}]+")).anyMatch(blockedTerms::contains);
    }

    static String normalizeForDuplicates(String body) {
        return body.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    static String unLeet(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replace('0', 'o').replace('1', 'i').replace('3', 'e').replace('4', 'a')
                .replace('5', 's').replace('@', 'a').replace('$', 's');
    }

    private static Set<String> load(String path) {
        try (var in = new ClassPathResource(path).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .map(String::strip)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .map(StoryContentGuard::unLeet)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't read " + path, e);
        }
    }
}
