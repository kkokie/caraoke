package com.caraoke.follow;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    /** Idempotent: double taps never error or double count. */
    @Modifying
    @Query(value = "INSERT INTO follows (follower_id, followee_id) VALUES (:follower, :followee) ON CONFLICT DO NOTHING",
           nativeQuery = true)
    int insertIfAbsent(@Param("follower") long followerId, @Param("followee") long followeeId);

    @Modifying
    @Query(value = "DELETE FROM follows WHERE follower_id = :follower AND followee_id = :followee", nativeQuery = true)
    int deleteOne(@Param("follower") long followerId, @Param("followee") long followeeId);

    long countByIdFolloweeId(long userId);   // followers of userId

    long countByIdFollowerId(long userId);   // accounts userId follows

    /** Followers of a user, newest first. */
    List<Follow> findByIdFolloweeIdOrderByCreatedAtDesc(long userId, Limit limit);

    /** Accounts a user follows, newest first. */
    List<Follow> findByIdFollowerIdOrderByCreatedAtDesc(long userId, Limit limit);
}
