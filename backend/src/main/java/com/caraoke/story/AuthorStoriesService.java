package com.caraoke.story;

import com.caraoke.resonance.ResonanceService;
import com.caraoke.resonance.ResonanceSummary;
import com.caraoke.song.SongDtos.SongSummary;
import com.caraoke.song.SongService;
import com.caraoke.story.StoryDtos.AuthorStats;
import com.caraoke.story.StoryDtos.StoryTile;
import com.caraoke.story.StoryDtos.StoryTilePage;
import com.caraoke.story.StoryDtos.YearTile;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * One author's stories, shaped for a profile: the tile grid and the header stats.
 * Called by the profile feature with an internal user id (never an auth uid).
 */
@Service
public class AuthorStoriesService {

    static final int DEFAULT_GRID_PAGE = 30;   // 10 rows of 3
    static final int MAX_GRID_PAGE = 60;

    private final StoryRepository stories;
    private final SongService songs;
    private final ResonanceService resonances;
    private final StoryPhotos photos;

    public AuthorStoriesService(StoryRepository stories, SongService songs, ResonanceService resonances, StoryPhotos photos) {
        this.stories = stories;
        this.songs = songs;
        this.resonances = resonances;
        this.photos = photos;
    }

    @Transactional(readOnly = true)
    public StoryTilePage tiles(long authorId, Long beforeId, int requestedSize) {
        int size = clamp(requestedSize);
        List<Story> rows = stories.findByUserIdAndStatusAndIdLessThanOrderByIdDesc(
                authorId, StoryStatus.VISIBLE, beforeId == null ? Long.MAX_VALUE : beforeId, Limit.of(size + 1));
        boolean hasMore = rows.size() > size;
        List<Story> page = hasMore ? rows.subList(0, size) : rows;
        Long nextCursor = hasMore ? page.get(page.size() - 1).getId() : null;
        return new StoryTilePage(toTiles(page), nextCursor);
    }

    /**
     * Tiles for specific stories in the given order (e.g. "stories I felt").
     * Stories that were since hidden are skipped; deleted ones are already gone.
     */
    @Transactional(readOnly = true)
    public List<StoryTile> tilesFor(List<Long> storyIds) {
        if (storyIds.isEmpty()) return List.of();
        Map<Long, Story> byId = stories.findAllById(storyIds).stream()
                .filter(Story::isVisible)
                .collect(Collectors.toMap(Story::getId, s -> s));
        List<Story> ordered = storyIds.stream().map(byId::get).filter(Objects::nonNull).toList();
        return toTiles(ordered);
    }

    /** "My life in songs": up to 60 stories that have a memory year, oldest memory first. */
    @Transactional(readOnly = true)
    public List<YearTile> lifeInSongs(long authorId) {
        List<Story> rows = stories.findTop60ByUserIdAndStatusAndYearOfMemoryNotNullOrderByYearOfMemoryAscIdAsc(
                authorId, StoryStatus.VISIBLE);
        List<StoryTile> tiles = toTiles(rows);
        return java.util.stream.IntStream.range(0, rows.size())
                .mapToObj(i -> new YearTile(rows.get(i).getYearOfMemory(), tiles.get(i)))
                .toList();
    }

    /** Whether this is one of the author's own stories that others can see (for pinning). */
    @Transactional(readOnly = true)
    public boolean isVisibleStoryBy(long authorId, long storyId) {
        return stories.existsByIdAndUserIdAndStatus(storyId, authorId, StoryStatus.VISIBLE);
    }

    /** Header numbers: how many stories, and how many times people felt them. */
    @Transactional(readOnly = true)
    public AuthorStats stats(long authorId) {
        List<Long> ids = stories.findIdsByUserIdAndStatus(authorId, StoryStatus.VISIBLE);
        return new AuthorStats(ids.size(), resonances.totalFor(ids));
    }

    private List<StoryTile> toTiles(List<Story> page) {
        if (page.isEmpty()) return List.of();
        Map<Long, SongSummary> songById = songs.findSummaries(songIds(page));
        List<Long> ids = page.stream().map(Story::getId).toList();
        Map<Long, ResonanceSummary> felt = resonances.summaries(ids, null);
        Map<Long, StoryDtos.PhotoView> covers = photos.covers(ids);
        return page.stream().map(s -> toTile(s, songById.get(s.getSongId()), felt, covers.get(s.getId()))).toList();
    }

    /** The tile is the story's photo when it has one, else the album cover. */
    private static StoryTile toTile(Story s, SongSummary song, Map<Long, ResonanceSummary> felt, StoryDtos.PhotoView photo) {
        return new StoryTile(
                s.getId(),
                s.getSongId(),
                song == null ? null : song.title(),
                song == null ? null : song.artist(),
                photo != null ? photo.url() : song == null ? null : song.artworkUrl(),
                felt.getOrDefault(s.getId(), ResonanceSummary.NONE).count());
    }

    private static Set<Long> songIds(List<Story> page) {
        return page.stream().map(Story::getSongId).collect(Collectors.toSet());
    }

    private static int clamp(int requested) {
        if (requested <= 0) return DEFAULT_GRID_PAGE;
        return Math.min(requested, MAX_GRID_PAGE);
    }
}
