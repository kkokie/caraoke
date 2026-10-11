package com.caraoke.profile;

import com.caraoke.block.BlockService;
import com.caraoke.common.ApiErrors;
import com.caraoke.follow.FollowService;
import com.caraoke.profile.ProfileDtos.BlockedAccount;
import com.caraoke.user.UserDtos.Author;
import com.caraoke.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Block / unblock by @handle. Blocking also ends follows in both directions, so neither person
 * keeps showing up in the other's lists. Unblocking doesn't restore them.
 */
@Service
public class ProfileBlockService {

    private final UserService users;
    private final BlockService blocks;
    private final FollowService follows;

    public ProfileBlockService(UserService users, BlockService blocks, FollowService follows) {
        this.users = users;
        this.blocks = blocks;
        this.follows = follows;
    }

    @Transactional
    public void block(String viewerAuthUid, String handle) {
        long viewer = requireViewer(viewerAuthUid), target = requireUser(handle);
        blocks.block(viewer, target);
        follows.unfollow(viewer, target);
        follows.unfollow(target, viewer);
    }

    @Transactional
    public void unblock(String viewerAuthUid, String handle) {
        blocks.unblock(requireViewer(viewerAuthUid), requireUser(handle));
    }

    /** "Blocked accounts" in settings, newest first. */
    public List<BlockedAccount> blockedAccounts(String viewerAuthUid) {
        List<Long> ids = blocks.blockedBy(requireViewer(viewerAuthUid));
        Map<Long, Author> authors = users.findAuthors(ids);
        return ids.stream().map(authors::get).filter(Objects::nonNull)
                .map(a -> new BlockedAccount(a.handle(), a.displayName(), a.avatarUrl()))
                .toList();
    }

    private long requireViewer(String authUid) {
        return users.findUserId(authUid).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
    }

    private long requireUser(String handle) {
        return users.findUserIdByHandle(handle).orElseThrow(() -> ApiErrors.notFound("User not found"));
    }
}
