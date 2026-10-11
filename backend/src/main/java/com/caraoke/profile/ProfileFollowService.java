package com.caraoke.profile;

import com.caraoke.block.BlockService;
import com.caraoke.common.ApiErrors;
import com.caraoke.follow.FollowService;
import com.caraoke.user.UserDtos.Author;
import com.caraoke.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Follow / unfollow by @handle, and the followers / following lists. */
@Service
public class ProfileFollowService {

    static final int LIST_LIMIT = 100;

    private final UserService users;
    private final FollowService follows;
    private final BlockService blocks;

    public ProfileFollowService(UserService users, FollowService follows, BlockService blocks) {
        this.blocks = blocks;
        this.users = users;
        this.follows = follows;
    }

    public FollowService.State follow(String viewerAuthUid, String handle) {
        long viewer = requireViewer(viewerAuthUid), target = requireUser(handle);
        if (blocks.eitherBlocked(viewer, target)) throw ApiErrors.notFound("User not found");
        return follows.follow(viewer, target);
    }

    public FollowService.State unfollow(String viewerAuthUid, String handle) {
        return follows.unfollow(requireViewer(viewerAuthUid), requireUser(handle));
    }

    public List<Author> followers(String viewerAuthUid, String handle) {
        return toAuthors(viewerAuthUid, follows.followerIds(requireVisibleUser(viewerAuthUid, handle), LIST_LIMIT));
    }

    public List<Author> following(String viewerAuthUid, String handle) {
        return toAuthors(viewerAuthUid, follows.followingIds(requireVisibleUser(viewerAuthUid, handle), LIST_LIMIT));
    }

    /** Keeps the newest-first order from the follow graph; people hidden from the viewer by a block are left out. */
    private List<Author> toAuthors(String viewerAuthUid, List<Long> allIds) {
        var hidden = blocks.hiddenFrom(users.findUserId(viewerAuthUid).orElse(null));
        List<Long> ids = allIds.stream().filter(id -> !hidden.contains(id)).toList();
        Map<Long, Author> authors = users.findAuthors(ids);
        return ids.stream().map(authors::get).filter(Objects::nonNull).toList();
    }

    private long requireViewer(String authUid) {
        return users.findUserId(authUid).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
    }

    /** 404 when that person blocked the viewer, like their profile. */
    private long requireVisibleUser(String viewerAuthUid, String handle) {
        long userId = requireUser(handle);
        Long viewerId = users.findUserId(viewerAuthUid).orElse(null);
        if (viewerId != null && blocks.hasBlocked(userId, viewerId)) throw ApiErrors.notFound("User not found");
        return userId;
    }

    private long requireUser(String handle) {
        return users.findUserIdByHandle(handle).orElseThrow(() -> ApiErrors.notFound("User not found"));
    }
}
