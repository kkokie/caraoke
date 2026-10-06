package com.caraoke.story;

import com.caraoke.common.ApiErrors;
import com.caraoke.song.SongDtos.SongView;
import com.caraoke.song.SongService;
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

    public StoryService(StoryRepository stories, SongService songs, UserService users, StoryViewAssembler views) {
        this.stories = stories;
        this.songs = songs;
        this.users = users;
        this.views = views;
    }

    @Transactional
    public StoryView post(String authUid, long songId, StoryInput req) {
        long userId = requireProfile(authUid);
        SongView song = songs.get(songId);                       // 404 if the song doesn't exist
        Story story = stories.save(new Story(
                userId,
                songId,
                StoryRules.body(req.body()),
                StoryRules.moment(req.momentSec(), song.durationSec()),
                StoryRules.year(req.yearOfMemory(), Year.now())));
        return views.toView(story, userId);
    }

    @Transactional(readOnly = true)
    public StoryPage feed(String authUid, long songId, Long beforeId, int requestedSize) {
        songs.get(songId);                                       // 404 for unknown songs, not an empty feed
        int size = clampPageSize(requestedSize);
        List<Story> rows = stories.findBySongIdAndStatusAndIdLessThanOrderByIdDesc(
                songId, StoryStatus.VISIBLE, beforeId == null ? Long.MAX_VALUE : beforeId, Limit.of(size + 1));
        return toPage(rows, size, users.findUserId(authUid).orElse(null));
    }

    /** A single story (story page, edit screen). Hidden stories are visible only to their author. */
    @Transactional(readOnly = true)
    public StoryView get(String authUid, long storyId) {
        Long viewerId = users.findUserId(authUid).orElse(null);
        Story story = stories.findById(storyId)
                .filter(s -> s.isVisible() || (viewerId != null && s.isWrittenBy(viewerId)))
                .orElseThrow(() -> ApiErrors.notFound("Story not found"));
        return views.toView(story, viewerId);
    }

    /** Authors can rewrite their story, moment, and year. The song stays fixed. */
    @Transactional
    public StoryView edit(String authUid, long storyId, StoryInput req) {
        long userId = requireProfile(authUid);
        Story story = requireOwnStory(storyId, userId);
        SongView song = songs.get(story.getSongId());
        story.edit(
                StoryRules.body(req.body()),
                StoryRules.moment(req.momentSec(), song.durationSec()),
                StoryRules.year(req.yearOfMemory(), Year.now()));
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
