package com.caraoke.story;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {

    /** Keyset page: newest first, strictly older than the cursor id. Backed by idx_stories_song_feed. */
    List<Story> findBySongIdAndStatusAndIdLessThanOrderByIdDesc(
            long songId, StoryStatus status, long beforeId, Limit limit);
}
