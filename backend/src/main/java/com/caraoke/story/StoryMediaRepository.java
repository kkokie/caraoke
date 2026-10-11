package com.caraoke.story;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StoryMediaRepository extends JpaRepository<StoryMedia, Long> {

    /** The cover photo (position 0) for a page of stories, in one query. */
    List<StoryMedia> findByStoryIdInAndPosition(Collection<Long> storyIds, short position);

    Optional<StoryMedia> findByStoryIdAndPosition(long storyId, short position);

    List<StoryMedia> findByStoryId(long storyId);
}
