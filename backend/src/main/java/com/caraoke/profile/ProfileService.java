package com.caraoke.profile;

import com.caraoke.block.BlockService;
import com.caraoke.common.ApiErrors;
import com.caraoke.follow.FollowService;
import com.caraoke.profile.ProfileDtos.ProfileView;
import com.caraoke.profile.ProfileDtos.SocialView;
import com.caraoke.story.AuthorStoriesService;
import com.caraoke.story.StoryDtos.StoryTilePage;
import com.caraoke.user.UserService;
import org.springframework.stereotype.Service;

/**
 * The profile dashboard is a composition feature: it sits ABOVE user, story and follow and reads
 * from all three (user can't call story, since story already calls user; this keeps the graph acyclic).
 */
@Service
public class ProfileService {

    private final UserService users;
    private final AuthorStoriesService authorStories;
    private final FollowService follows;
    private final BlockService blocks;

    public ProfileService(UserService users, AuthorStoriesService authorStories, FollowService follows, BlockService blocks) {
        this.blocks = blocks;
        this.users = users;
        this.authorStories = authorStories;
        this.follows = follows;
    }

    public ProfileView profile(String viewerAuthUid, String handle) {
        long userId = requireUser(handle);
        Long viewerId = users.findUserId(viewerAuthUid).orElse(null);
        boolean me = viewerId != null && viewerId == userId;
        // If they blocked you, their profile doesn't exist for you
        if (viewerId != null && !me && blocks.hasBlocked(userId, viewerId)) throw ApiErrors.notFound("User not found");
        boolean blockedByMe = viewerId != null && !me && blocks.hasBlocked(viewerId, userId);
        return new ProfileView(
                users.getByHandle(handle),
                authorStories.stats(userId),
                social(userId, viewerId, me),
                me,
                blockedByMe);
    }

    /** A blocked profile (either way) shows no stories. */
    public StoryTilePage stories(String viewerAuthUid, String handle, Long beforeId, int limit) {
        long userId = requireUser(handle);
        Long viewerId = users.findUserId(viewerAuthUid).orElse(null);
        if (blocks.eitherBlocked(viewerId, userId)) return new StoryTilePage(java.util.List.of(), null);
        return authorStories.tiles(userId, beforeId, limit);
    }

    private SocialView social(long userId, Long viewerId, boolean me) {
        FollowService.Counts counts = follows.counts(userId);
        boolean followedByMe = viewerId != null && !me && follows.isFollowing(viewerId, userId);
        return new SocialView(counts.followers(), counts.following(), followedByMe);
    }

    private long requireUser(String handle) {
        return users.findUserIdByHandle(handle).orElseThrow(() -> ApiErrors.notFound("User not found"));
    }
}
