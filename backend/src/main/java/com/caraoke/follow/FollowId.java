package com.caraoke.follow;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/** Composite key: follower -> followee, at most once. */
@Embeddable
public class FollowId implements Serializable {

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "followee_id", nullable = false)
    private Long followeeId;

    protected FollowId() { }  // JPA

    FollowId(long followerId, long followeeId) {
        this.followerId = followerId;
        this.followeeId = followeeId;
    }

    public Long getFollowerId() { return followerId; }
    public Long getFolloweeId() { return followeeId; }

    @Override
    public boolean equals(Object o) {
        return o instanceof FollowId other
                && Objects.equals(followerId, other.followerId)
                && Objects.equals(followeeId, other.followeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(followerId, followeeId);
    }
}
