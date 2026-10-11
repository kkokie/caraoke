package com.caraoke.story;

import com.caraoke.block.BlockService;
import com.caraoke.common.ApiErrors;
import com.caraoke.song.SongDtos.SongView;
import com.caraoke.song.SongService;
import com.caraoke.story.StoryDtos.ShareQuota;
import com.caraoke.story.StoryDtos.StoryInput;
import com.caraoke.story.StoryDtos.StoryPage;
import com.caraoke.story.StoryDtos.StoryView;
import com.caraoke.user.UserService;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

/**
 * Stories on a song. Talks to the song and user features only through their services.
 * Resonance ("I felt this too") actions live in StoryResonanceService.
 */
@Service
public class StoryService {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 50;

    private final StoryRepository stories;
    private final SongService songs;
    private final UserService users;
    private final StoryViewAssembler views;
    private final DailyShareLimit dailyLimit;
    private final BlockService blocks;
    private final StoryContentGuard guard;

    public StoryService(StoryRepository stories, SongService songs, UserService users,
                        StoryViewAssembler views, DailyShareLimit dailyLimit, BlockService blocks,
                        StoryContentGuard guard) {
        this.blocks = blocks;
        this.guard = guard;
        this.stories = stories;
        this.songs = songs;
        this.users = users;
        this.views = views;
        this.dailyLimit = dailyLimit;
    }

    @Transactional
    public StoryView post(String authUid, long songId, StoryInput req) {
        long userId = requireProfile(authUid);
        SongView song = songs.get(songId);                       // 404 if the song doesn't exist
        Story story = new Story(
                userId,
                songId,
                StoryRules.body(req.body()),
                StoryRules.moment(req.momentSec(), song.durationSec()),
                StoryRules.year(req.yearOfMemory(), Year.now()));
        story.style(StoryRules.lyric(req.lyricQuote()), StoryRules.paper(req.paper()));
        guard.check(story.getBody(), story.getLyricQuote(), null);
        dailyLimit.check(userId);                                // 429 if today's story is already shared
        return views.toView(stories.save(story), userId);
    }

    /** Whether you can share today (the app shows "next story in 6h" instead of a failing form). */
    @Transactional(readOnly = true)
    public ShareQuota quota(String authUid) {
        return dailyLimit.status(requireProfile(authUid));
    }

    @Transactional(readOnly = true)
    public StoryPage feed(String authUid, long songId, Long beforeId, int requestedSize) {
        return feed(authUid, songId, beforeId, requestedSize, null);
    }

    /** `moment` (seconds) narrows the feed to that dot on the moments timeline (its 5-second slot). */
    @Transactional(readOnly = true)
    public StoryPage feed(String authUid, long songId, Long beforeId, int requestedSize, Integer moment) {
        songs.get(songId);                                       // 404 for unknown songs, not an empty feed
        int size = clampPageSize(requestedSize);
        Long viewerId = users.findUserId(authUid).orElse(null);
        long before = beforeId == null ? Long.MAX_VALUE : beforeId;
        List<Long> hidden = BlockService.notInList(blocks.hiddenFrom(viewerId));
        List<Story> rows = moment == null
                ? stories.findBySongIdAndStatusAndIdLessThanAndUserIdNotInOrderByIdDesc(
                        songId, StoryStatus.VISIBLE, before, hidden, Limit.of(size + 1))
                : momentRows(songId, StoryRules.momentSlot(moment), before, hidden, size + 1);
        return toPage(rows, size, viewerId);
    }

    private List<Story> momentRows(long songId, int slot, long before, List<Long> hidden, int limit) {
        return stories.findBySongIdAndStatusAndMomentSecBetweenAndIdLessThanAndUserIdNotInOrderByIdDesc(
                songId, StoryStatus.VISIBLE, slot, slot + StoryRules.MOMENT_SLOT_SEC - 1, before, hidden, Limit.of(limit));
    }

    /** Dots for the moments timeline: how many stories pin each 5-second slot. */
    @Transactional(readOnly = true)
    public List<StoryDtos.MomentCount> moments(long songId) {
        songs.get(songId);                                       // 404 for unknown songs
        return stories.countMoments(songId).stream()
                .map(m -> new StoryDtos.MomentCount(m.getSec(), m.getStories()))
                .toList();
    }

    /** A single story (story page, edit screen). Hidden stories are visible only to their author. */
    @Transactional(readOnly = true)
    public StoryView get(String authUid, long storyId) {
        Long viewerId = users.findUserId(authUid).orElse(null);
        Story story = stories.findById(storyId)
                .filter(s -> s.isVisible() || (viewerId != null && s.isWrittenBy(viewerId)))
                .filter(s -> !blocks.eitherBlocked(viewerId, s.getUserId()))   // blocked either way: it doesn't exist
                .orElseThrow(() -> ApiErrors.notFound("Story not found"));
        return views.toView(story, viewerId);
    }

    /** Authors can rewrite their story, moment, and year. The song stays fixed. */
    @Transactional
    public StoryView edit(String authUid, long storyId, StoryInput req) {
        long userId = requireProfile(authUid);
        Story story = requireOwnStory(storyId, userId);
        if (story.isRemoved()) throw ApiErrors.forbidden("This story was removed for breaking the Community Guidelines.");
        SongView song = songs.get(story.getSongId());
        story.edit(
                StoryRules.body(req.body()),
                StoryRules.moment(req.momentSec(), song.durationSec()),
                StoryRules.year(req.yearOfMemory(), Year.now()));
        story.style(StoryRules.lyric(req.lyricQuote()), StoryRules.paper(req.paper()));
        guard.check(story.getBody(), story.getLyricQuote(), story.getId());
        return views.toView(story, userId);                      // dirty checking saves on commit
    }

    @Transactional
    public void delete(String authUid, long storyId) {
        Story story = requireOwnStory(storyId, requireProfile(authUid));
        stories.delete(story);                                   // resonances/replies cascade in the DB
    }

    // ---- helpers ---------------------------------------------------------------

    private long requireProfile(String authUid) {
        return users.findUserId(authUid).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
    }

    private Story requireOwnStory(long storyId, long userId) {
        Story story = stories.findById(storyId).orElseThrow(() -> ApiErrors.notFound("Story not found"));
        if (!story.isWrittenBy(userId)) throw ApiErrors.forbidden("You can only change your own stories.");
        return story;
    }

    /** We fetched size+1 rows: the extra one only tells us whether another page exists. */
    private StoryPage toPage(List<Story> rows, int size, Long viewerId) {
        boolean hasMore = rows.size() > size;
        List<Story> page = hasMore ? rows.subList(0, size) : rows;
        Long nextCursor = hasMore ? page.get(page.size() - 1).getId() : null;
        return new StoryPage(views.toViews(page, viewerId), nextCursor);
    }

    private static int clampPageSize(int requested) {
        if (requested <= 0) return DEFAULT_PAGE_SIZE;
        return Math.min(requested, MAX_PAGE_SIZE);
    }
}
