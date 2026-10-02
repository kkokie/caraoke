package com.caraoke.story;

import com.caraoke.user.UserDtos.Author;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/** Request/response shapes for the story API. */
public final class StoryDtos {

    private StoryDtos() { }

    /** Body is checked in StoryRules (needs trimming first); the year needs "today" to validate. */
    public record PostStoryRequest(
            @NotNull String body,
            Integer momentSec,
            Integer yearOfMemory) { }

    public record StoryView(
            long id,
            long songId,
            Author author,
            String body,
            Integer momentSec,
            Integer yearOfMemory,
            Instant createdAt,
            boolean mine) {

        static StoryView of(Story s, Author author, boolean mine) {
            return new StoryView(
                    s.getId(), s.getSongId(), author, s.getBody(), s.getMomentSec(),
                    s.getYearOfMemory() == null ? null : s.getYearOfMemory().intValue(),
                    s.getCreatedAt(), mine);
        }
    }

    /** One page of a feed. Pass nextCursor back as ?before= to get the next page; null = no more. */
    public record StoryPage(List<StoryView> items, Long nextCursor) { }
}
