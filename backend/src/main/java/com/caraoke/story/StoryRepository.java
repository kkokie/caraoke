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

    /** Your most recent story, any status (for the one-a-day rule). Backed by idx_stories_user_created (V1). */
    Optional<Story> findFirstByUserIdOrderByCreatedAtDesc(long userId);

    // ---- Discover -----------------------------------------------------------

    /**
     * One random visible story worth finding: long enough to be a real story, not the viewer's,
     * and not one they've already dug up this session. `exclude` is a Postgres array literal ("{1,2}").
     * ORDER BY random() is fine at our size; swap for TABLESAMPLE or a random key when it isn't.
     */
    @Query(value = """
            SELECT * FROM stories
            WHERE status = 'VISIBLE'
              AND char_length(body) >= :minChars
              AND user_id <> :viewerId
              AND id <> ALL (CAST(:exclude AS bigint[]))
            ORDER BY random()
            LIMIT 1
            """, nativeQuery = true)
    Optional<Story> findRandomForDig(@Param("viewerId") long viewerId,
                                     @Param("minChars") int minChars,
                                     @Param("exclude") String exclude);

    /** Years that have stories, newest first, for the "dig through the years" strip. */
    @Query(value = """
            SELECT CAST(year_of_memory AS int) AS year, count(*) AS stories FROM stories
            WHERE status = 'VISIBLE' AND year_of_memory IS NOT NULL
            GROUP BY year_of_memory ORDER BY year_of_memory DESC
            """, nativeQuery = true)
    List<YearCount> countByYear();

    interface YearCount {
        int getYear();
        long getStories();
    }

    /** One year's stories, keyset paged newest first. Backed by idx_stories_year_feed. */
    List<Story> findByYearOfMemoryAndStatusAndIdLessThanOrderByIdDesc(
            short yearOfMemory, StoryStatus status, long beforeId, Limit limit);

    /** Songs with the most stories ("songs full of stories"). */
    @Query(value = """
            SELECT song_id AS songId, count(*) AS stories FROM stories
            WHERE status = 'VISIBLE'
            GROUP BY song_id ORDER BY count(*) DESC, song_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<SongCount> topSongs(@Param("limit") int limit);

    interface SongCount {
        long getSongId();
        long getStories();
    }

    long countByUserIdAndStatus(long userId, StoryStatus status);

    @Query("select s.id from Story s where s.userId = :userId and s.status = :status")
    List<Long> findIdsByUserIdAndStatus(@Param("userId") long userId, @Param("status") StoryStatus status);
}
