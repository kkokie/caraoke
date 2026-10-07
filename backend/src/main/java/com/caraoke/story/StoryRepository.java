package com.caraoke.story;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {

    /** Keyset page: newest first, strictly older than the cursor id. Backed by idx_stories_song_feed. */
    List<Story> findBySongIdAndStatusAndIdLessThanOrderByIdDesc(
            long songId, StoryStatus status, long beforeId, Limit limit);

    /** One author's stories for their profile grid. Backed by idx_stories_user_feed. */
    List<Story> findByUserIdAndStatusAndIdLessThanOrderByIdDesc(
            long userId, StoryStatus status, long beforeId, Limit limit);

    /** Your most recent story, any status (for the one-a-day rule). Backed by idx_stories_user_created. */
    Optional<Story> findFirstByUserIdOrderByCreatedAtDesc(long userId);

    long countByUserIdAndStatus(long userId, StoryStatus status);

    @Query("select s.id from Story s where s.userId = :userId and s.status = :status")
    List<Long> findIdsByUserIdAndStatus(@Param("userId") long userId, @Param("status") StoryStatus status);
}
