package com.caraoke.story;

import com.caraoke.song.SongDtos.SongSummary;
import com.caraoke.story.StoryDtos.StoryView;

import java.util.List;

/** Shapes for Discover: stories found away from their song page carry their song with them. */
public final class DiscoverDtos {

    private DiscoverDtos() { }

    public record FoundStory(StoryView story, SongSummary song) { }

    /** Keyset page of found stories; pass nextCursor back as ?before=. */
    public record FoundStoryPage(List<FoundStory> items, Long nextCursor) { }

    public record YearCount(int year, long stories) { }

    public record SongWithCount(SongSummary song, long stories) { }
}
