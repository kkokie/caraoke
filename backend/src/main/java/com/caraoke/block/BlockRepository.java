package com.caraoke.block;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, BlockId> {

    @Modifying
    @Query(value = "INSERT INTO blocks (blocker_id, blocked_id) VALUES (:blocker, :blocked) ON CONFLICT DO NOTHING",
           nativeQuery = true)
    int insertIfAbsent(@Param("blocker") long blockerId, @Param("blocked") long blockedId);

    @Modifying
    @Query(value = "DELETE FROM blocks WHERE blocker_id = :blocker AND blocked_id = :blocked", nativeQuery = true)
    int deleteOne(@Param("blocker") long blockerId, @Param("blocked") long blockedId);

    /** Everyone hidden from this user: people they blocked and people who blocked them. */
    @Query(value = """
            SELECT blocked_id FROM blocks WHERE blocker_id = :userId
            UNION
            SELECT blocker_id FROM blocks WHERE blocked_id = :userId
            """, nativeQuery = true)
    List<Long> findHiddenFrom(@Param("userId") long userId);

    @Query(value = """
            SELECT EXISTS (SELECT 1 FROM blocks
                           WHERE (blocker_id = :a AND blocked_id = :b) OR (blocker_id = :b AND blocked_id = :a))
            """, nativeQuery = true)
    boolean existsEitherWay(@Param("a") long a, @Param("b") long b);

    /** Accounts a user has blocked, newest first (for "Blocked accounts"). */
    List<Block> findByIdBlockerIdOrderByCreatedAtDesc(long blockerId);
}
