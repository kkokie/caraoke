package com.caraoke.story;

import com.caraoke.common.ApiErrors;
import com.caraoke.song.SongDtos.SongView;
import com.caraoke.song.SongService;
import com.caraoke.story.StoryDtos.PostStoryRequest;
import com.caraoke.story.StoryDtos.StoryPage;
import com.caraoke.story.StoryDtos.StoryView;
import com.caraoke.user.UserDtos.Author;
import com.caraoke.user.UserService;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stories on a song. Talks to the song and user features only through their services.
 */
@Service
public class StoryService {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 50;

    private final StoryRepository stories;
    private final SongService songs;
    private final UserService users;

    public StoryService(StoryRepository stories, SongService songs, UserService users) {
        this.stories = stories;
        this.songs = songs;
        this.users = users;
    }

    @Transactional
    public StoryView post(String authUid, long songId, PostStoryRequest req) {
        long userId = requireProfile(authUid);
        SongView song = songs.get(songId);                       // 404 if the song doesn't exist
        Story story = stories.save(new Story(
                userId,
                songId,
                StoryRules.body(req.body()),
                StoryRules.moment(req.momentSec(), song.durationSec()),
                StoryRules.year(req.yearOfMemory(), Year.now())));
        return toViews(List.of(story), userId).get(0);
    }

    @Transactional(readOnly = true)
    public StoryPage feed(String authUid, long songId, Long beforeId, int requestedSize) {
        songs.get(songId);                                       // 404 for unknown songs, not an empty feed
        int size = clampPageSize(requestedSize);
        List<Story> rows = stories.findBySongIdAndStatusAndIdLessThanOrderByIdDesc(
                songId, StoryStatus.VISIBLE, beforeId == null ? Long.MAX_VALUE : beforeId, Limit.of(size + 1));
        return toPage(rows, size, users.findUserId(authUid).orElse(null));
    }

    @Transactional
    public void delete(String authUid, long storyId) {
        long userId = requireProfile(authUid);
        Story story = stories.findById(storyId).orElseThrow(() -> ApiErrors.notFound("Story not found"));
        if (!story.isWrittenBy(userId)) throw ApiErrors.forbidden("You can only delete your own stories.");
        stories.delete(story);                                   // resonances/replies cascade in the DB
    }

    // ---- helpers ---------------------------------------------------------------

    private long requireProfile(String authUid) {
        return users.findUserId(authUid).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
    }

    /** We fetched size+1 rows: the extra one only tells us whether another page exists. */
    private StoryPage toPage(List<Story> rows, int size, Long viewerId) {
        boolean hasMore = rows.size() > size;
        List<Story> page = hasMore ? rows.subList(0, size) : rows;
        Long nextCursor = hasMore ? page.get(page.size() - 1).getId() : null;
        return new StoryPage(toViews(page, viewerId), nextCursor);
    }

    private List<StoryView> toViews(List<Story> page, Long viewerId) {
        Set<Long> authorIds = page.stream().map(Story::getUserId).collect(Collectors.toSet());
        Map<Long, Author> authors = users.findAuthors(authorIds);   // one query for the whole page
        return page.stream()
                .map(s -> StoryView.of(s, authors.get(s.getUserId()), viewerId != null && s.isWrittenBy(viewerId)))
                .toList();
    }

    private static int clampPageSize(int requested) {
        if (requested <= 0) return DEFAULT_PAGE_SIZE;
        return Math.min(requested, MAX_PAGE_SIZE);
    }
}
