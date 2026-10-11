package com.caraoke.profile;

import com.caraoke.block.BlockService;
import com.caraoke.common.ApiErrors;
import com.caraoke.page.PageService;
import com.caraoke.page.PageSettings;
import com.caraoke.profile.ProfilePageDtos.PageInput;
import com.caraoke.profile.ProfilePageDtos.PageView;
import com.caraoke.song.SongDtos.SongSummary;
import com.caraoke.song.SongService;
import com.caraoke.story.AuthorStoriesService;
import com.caraoke.story.StoryDtos.StoryTile;
import com.caraoke.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Your decorated page (profile -> page, song, story, user, block). The page feature stores ids;
 * this checks they're real (and that a pinned story is yours) and turns them into songs and stories.
 */
@Service
public class ProfilePageService {

    private final UserService users;
    private final PageService pages;
    private final SongService songs;
    private final AuthorStoriesService authorStories;
    private final BlockService blocks;

    public ProfilePageService(UserService users, PageService pages, SongService songs,
                              AuthorStoriesService authorStories, BlockService blocks) {
        this.users = users;
        this.pages = pages;
        this.songs = songs;
        this.authorStories = authorStories;
        this.blocks = blocks;
    }

    @Transactional(readOnly = true)
    public PageView page(String viewerAuthUid, String handle) {
        long ownerId = users.findUserIdByHandle(handle).orElseThrow(() -> ApiErrors.notFound("User not found"));
        users.getByHandle(handle);                                   // 404 for banned accounts too
        Long viewerId = users.findUserId(viewerAuthUid).orElse(null);
        if (viewerId != null && viewerId != ownerId && blocks.hasBlocked(ownerId, viewerId)) {
            throw ApiErrors.notFound("User not found");             // they blocked you: no page
        }
        boolean showStories = !blocks.eitherBlocked(viewerId, ownerId);
        return view(ownerId, pages.settings(ownerId), showStories);
    }

    @Transactional
    public PageView save(String authUid, PageInput in) {
        long me = requireProfile(authUid);
        requireSongs(songIdsOf(in));
        if (in.pinnedStoryId() != null && !authorStories.isVisibleStoryBy(me, in.pinnedStoryId())) {
            throw ApiErrors.badRequest("You can only pin one of your own stories.");
        }
        PageSettings saved = pages.save(me, new PageSettings(in.theme(), in.background(), in.type(),
                in.pageSongId(), in.pinnedStoryId(), in.blocks(), in.raisedSongIds()));
        return view(me, saved, true);
    }

    @Transactional
    public PageView setPageSong(String authUid, Long songId) {
        long me = requireProfile(authUid);
        if (songId != null) requireSongs(Set.of(songId));
        pages.setPageSong(me, songId);
        return view(me, pages.settings(me), true);
    }

    // ---- assembly --------------------------------------------------------------

    private PageView view(long ownerId, PageSettings s, boolean showStories) {
        Map<Long, SongSummary> raised = songs.findSummaries(s.raisedSongIds());
        return new PageView(
                s.theme(), s.background(), s.type(), s.blocks(),
                s.pageSongId() == null ? null : songs.get(s.pageSongId()),
                showStories ? pinned(s.pinnedStoryId()) : null,
                showStories ? authorStories.lifeInSongs(ownerId) : List.of(),
                s.raisedSongIds().stream().map(raised::get).filter(Objects::nonNull).toList());
    }

    /** A pinned story that was since hidden or removed just disappears from the page. */
    private StoryTile pinned(Long storyId) {
        if (storyId == null) return null;
        List<StoryTile> tiles = authorStories.tilesFor(List.of(storyId));
        return tiles.isEmpty() ? null : tiles.get(0);
    }

    private void requireSongs(Set<Long> ids) {
        if (ids.isEmpty()) return;
        if (songs.findSummaries(ids).size() != ids.size()) throw ApiErrors.badRequest("That song isn't in the catalog yet.");
    }

    private static Set<Long> songIdsOf(PageInput in) {
        Set<Long> ids = new HashSet<>();
        if (in.pageSongId() != null) ids.add(in.pageSongId());
        if (in.raisedSongIds() != null) in.raisedSongIds().stream().filter(Objects::nonNull).forEach(ids::add);
        return ids;
    }

    private long requireProfile(String authUid) {
        return users.findUserId(authUid).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
    }
}
