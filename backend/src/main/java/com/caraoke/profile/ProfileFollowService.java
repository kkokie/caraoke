package com.caraoke.profile;

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

    public ProfileFollowService(UserService users, FollowService follows) {
        this.users = users;
        this.follows = follows;
    }

    public FollowService.State follow(String viewerAuthUid, String handle) {
        return follows.follow(requireViewer(viewerAuthUid), requireUser(handle));
    }

    public FollowService.State unfollow(String viewerAuthUid, String handle) {
        return follows.unfollow(requireViewer(viewerAuthUid), requireUser(handle));
    }

    public List<Author> followers(String handle) {
        return toAuthors(follows.followerIds(requireUser(handle), LIST_LIMIT));
    }

    public List<Author> following(String handle) {
        return toAuthors(follows.followingIds(requireUser(handle), LIST_LIMIT));
    }

    /** Keeps the newest-first order from the follow graph. */
    private List<Author> toAuthors(List<Long> ids) {
        Map<Long, Author> authors = users.findAuthors(ids);
        return ids.stream().map(authors::get).filter(Objects::nonNull).toList();
    }

    private long requireViewer(String authUid) {
        return users.findUserId(authUid).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
    }

    private long requireUser(String handle) {
        return users.findUserIdByHandle(handle).orElseThrow(() -> ApiErrors.notFound("User not found"));
    }
}
