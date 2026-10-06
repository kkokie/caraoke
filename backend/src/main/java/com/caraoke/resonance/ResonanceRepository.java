package com.caraoke.resonance;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ResonanceRepository extends JpaRepository<Resonance, ResonanceId> {

    /** Idempotent: tapping twice (or two devices at once) never errors or double-counts. */
    @Modifying
    @Query(value = "INSERT INTO resonances (user_id, story_id) VALUES (:userId, :storyId) ON CONFLICT DO NOTHING",
           nativeQuery = true)
    int insertIfAbsent(@Param("userId") long userId, @Param("storyId") long storyId);

    @Modifying
    @Query(value = "DELETE FROM resonances WHERE user_id = :userId AND story_id = :storyId", nativeQuery = true)
    int deleteOne(@Param("userId") long userId, @Param("storyId") long storyId);

    long countByIdStoryId(long storyId);

    /** Counts for a whole feed page in one query. Stories with zero resonances are simply absent. */
    @Query("select r.id.storyId as storyId, count(r) as total from Resonance r "
         + "where r.id.storyId in :storyIds group by r.id.storyId")
    List<StoryCount> countByStoryIds(@Param("storyIds") Collection<Long> storyIds);

    /** Which of these stories this user has resonated with. */
    @Query("select r.id.storyId from Resonance r where r.id.userId = :userId and r.id.storyId in :storyIds")
    List<Long> findStoryIdsResonatedBy(@Param("userId") long userId, @Param("storyIds") Collection<Long> storyIds);

    List<Resonance> findByIdStoryIdOrderByCreatedAtDesc(long storyId, Limit limit);

    interface StoryCount {
        Long getStoryId();
        Long getTotal();
    }
}
