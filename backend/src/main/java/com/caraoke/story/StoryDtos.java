package com.caraoke.story;

import com.caraoke.resonance.ResonanceSummary;
import com.caraoke.user.UserDtos.Author;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/** Request/response shapes for the story API. */
public final class StoryDtos {

    private StoryDtos() { }

    /**
     * What an author writes, used for both posting and editing.
     * Body is checked in StoryRules (needs trimming first); the year needs "today" to validate.
     */
    public record StoryInput(
            @NotNull String body,
            Integer momentSec,
            Integer yearOfMemory,
            String lyricQuote,        // optional: the line they felt
            String paper) {           // optional: "cream" (default), "dusk", "sage", "ink"

        /** Older clients send only the words, moment, and year. */
        public StoryInput(String body, Integer momentSec, Integer yearOfMemory) {
            this(body, momentSec, yearOfMemory, null, null);
        }
    }

    /** One story a day: whether you can share now, and if not, when you can. */
    public record ShareQuota(boolean canShare, Instant nextShareAt) { }

    public record StoryView(
            long id,
            long songId,
            Author author,
            String body,
            Integer momentSec,
            Integer yearOfMemory,
            String lyricQuote,
            String paper,
            Instant createdAt,
            Instant editedAt,         // null = never edited
            boolean mine,
            long resonanceCount,      // how many people felt this too
            boolean resonatedByMe,
            String review) {          // only on your own stories: "hidden" (in review) or "removed"; else null

        public StoryView(long id, long songId, Author author, String body, Integer momentSec, Integer yearOfMemory,
                         String lyricQuote, String paper, Instant createdAt, Instant editedAt, boolean mine,
                         long resonanceCount, boolean resonatedByMe) {
            this(id, songId, author, body, momentSec, yearOfMemory, lyricQuote, paper, createdAt, editedAt, mine,
                    resonanceCount, resonatedByMe, null);
        }

        static StoryView of(Story s, Author author, boolean mine, ResonanceSummary resonance) {
            return new StoryView(
                    s.getId(), s.getSongId(), author, s.getBody(), s.getMomentSec(),
                    s.getYearOfMemory() == null ? null : s.getYearOfMemory().intValue(),
                    s.getLyricQuote(), s.getPaper().apiValue(),
                    s.getCreatedAt(), s.getEditedAt(), mine, resonance.count(), resonance.mine(),
                    mine && !s.isVisible() ? s.getStatus().name().toLowerCase(java.util.Locale.ROOT) : null);
        }
    }

    /** What a moderator sees of a reported story. Carries internal ids: moderation only. */
    public record ModerationPreview(long id, long authorId, long songId, String body, String lyricQuote,
                                    StoryStatus status, Instant createdAt) { }

    /** A dot on the moments timeline: a 5-second slot starting at `sec`, and how many stories pin it. */
    public record MomentCount(int sec, long stories) { }

    /** One page of a feed. Pass nextCursor back as ?before= to get the next page; null = no more. */
    public record StoryPage(List<StoryView> items, Long nextCursor) { }

    /**
     * One square on a profile grid. coverUrl is the album art for now; once photos land,
     * it becomes the story's first photo, falling back to the album art.
     */
    public record StoryTile(
            long storyId,
            long songId,
            String songTitle,
            String artist,
            String coverUrl,
            long resonanceCount) { }

    public record StoryTilePage(List<StoryTile> items, Long nextCursor) { }

    /** Totals shown in a profile header. */
    public record AuthorStats(long stories, long felt) { }
}
