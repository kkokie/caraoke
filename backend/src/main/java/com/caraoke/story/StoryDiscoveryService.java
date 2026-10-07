package com.caraoke.story;

import com.caraoke.common.ApiErrors;
import com.caraoke.song.SongDtos.SongSummary;
import com.caraoke.song.SongService;
import com.caraoke.story.DiscoverDtos.FoundStory;
import com.caraoke.story.DiscoverDtos.FoundStoryPage;
import com.caraoke.story.DiscoverDtos.SongWithCount;
import com.caraoke.story.DiscoverDtos.YearCount;
import com.caraoke.story.StoryDtos.StoryView;
import com.caraoke.user.UserService;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Discover: treasure hunting for stories away from any one song. No ranking or "for you" yet,
 * just a quality floor (a real story, not a one-liner) and randomness.
 */
@Service
public class StoryDiscoveryService {

    static final int MIN_DIG_CHARS = 80;      // the quality floor for "dig again"
    static final int MAX_EXCLUDE = 200;       // stories already seen this session
    static final int TOP_SONGS = 10;
    static final int PAGE_SIZE = 20;

    private final StoryRepository stories;
    private final StoryViewAssembler views;
    private final SongService songs;
    private final UserService users;

    public StoryDiscoveryService(StoryRepository stories, StoryViewAssembler views, SongService songs, UserService users) {
        this.stories = stories;
        this.views = views;
        this.songs = songs;
        this.users = users;
    }

    /** A random good story you haven't seen yet this session; empty when there's nothing left. */
    @Transactional(readOnly = true)
    public Optional<FoundStory> dig(String authUid, Collection<Long> alreadySeen) {
        Long viewerId = users.findUserId(authUid).orElse(null);
        return stories.findRandomForDig(viewerId == null ? -1L : viewerId, MIN_DIG_CHARS, arrayLiteral(alreadySeen))
                .map(s -> found(List.of(s), viewerId).get(0));
    }

    @Transactional(readOnly = true)
    public List<YearCount> years() {
        return stories.countByYear().stream().map(y -> new YearCount(y.getYear(), y.getStories())).toList();
    }

    @Transactional(readOnly = true)
    public FoundStoryPage byYear(String authUid, int year, Long beforeId) {
        if (year < StoryRules.MIN_YEAR || year > 2999) throw ApiErrors.badRequest("That's not a year we have stories for.");
        Long viewerId = users.findUserId(authUid).orElse(null);
        List<Story> rows = stories.findByYearOfMemoryAndStatusAndIdLessThanOrderByIdDesc(
                (short) year, StoryStatus.VISIBLE, beforeId == null ? Long.MAX_VALUE : beforeId, Limit.of(PAGE_SIZE + 1));
        boolean hasMore = rows.size() > PAGE_SIZE;
        List<Story> page = hasMore ? rows.subList(0, PAGE_SIZE) : rows;
        Long next = hasMore ? page.get(page.size() - 1).getId() : null;
        return new FoundStoryPage(found(page, viewerId), next);
    }

    @Transactional(readOnly = true)
    public List<SongWithCount> songsFullOfStories() {
        List<StoryRepository.SongCount> top = stories.topSongs(TOP_SONGS);
        Map<Long, SongSummary> summaries = songs.findSummaries(top.stream().map(StoryRepository.SongCount::getSongId).toList());
        return top.stream()
                .filter(t -> summaries.containsKey(t.getSongId()))
                .map(t -> new SongWithCount(summaries.get(t.getSongId()), t.getStories()))
                .toList();
    }

    private List<FoundStory> found(List<Story> rows, Long viewerId) {
        List<StoryView> storyViews = views.toViews(rows, viewerId);
        Map<Long, SongSummary> songById = songs.findSummaries(rows.stream().map(Story::getSongId).collect(Collectors.toSet()));
        return storyViews.stream().map(v -> new FoundStory(v, songById.get(v.songId()))).toList();
    }

    /** "{1,2,3}" for Postgres; capped so a long session can't send an unbounded list. */
    static String arrayLiteral(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return "{}";
        return ids.stream().limit(MAX_EXCLUDE).map(String::valueOf).collect(Collectors.joining(",", "{", "}"));
    }
}
