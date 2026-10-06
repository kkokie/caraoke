package com.caraoke.follow;

import com.caraoke.common.ApiErrors;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Storage for the follow graph, by internal user id. Knows nothing about handles or profiles:
 * the profile feature resolves who's who and calls in here (profile -> follow, one way).
 */
@Service
public class FollowService {

    private final FollowRepository follows;

    public FollowService(FollowRepository follows) {
        this.follows = follows;
    }

    /** Followers/following totals for a profile header. */
    public record Counts(long followers, long following) { }

    /** What the Follow button needs after a tap. */
    public record State(boolean following, long followers) { }

    @Transactional
    public State follow(long followerId, long followeeId) {
        if (followerId == followeeId) throw ApiErrors.badRequest("You can't follow yourself.");
        follows.insertIfAbsent(followerId, followeeId);
        return new State(true, follows.countByIdFolloweeId(followeeId));
    }

    @Transactional
    public State unfollow(long followerId, long followeeId) {
        follows.deleteOne(followerId, followeeId);
        return new State(false, follows.countByIdFolloweeId(followeeId));
    }

    @Transactional(readOnly = true)
    public Counts counts(long userId) {
        return new Counts(follows.countByIdFolloweeId(userId), follows.countByIdFollowerId(userId));
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(long followerId, long followeeId) {
        return follows.existsById(new FollowId(followerId, followeeId));
    }

    @Transactional(readOnly = true)
    public List<Long> followerIds(long userId, int limit) {
        return follows.findByIdFolloweeIdOrderByCreatedAtDesc(userId, Limit.of(limit)).stream()
                .map(f -> f.getId().getFollowerId()).toList();
    }

    @Transactional(readOnly = true)
    public List<Long> followingIds(long userId, int limit) {
        return follows.findByIdFollowerIdOrderByCreatedAtDesc(userId, Limit.of(limit)).stream()
                .map(f -> f.getId().getFolloweeId()).toList();
    }
}
