package com.caraoke.story;

import com.caraoke.common.ApiErrors;
import com.caraoke.story.StoryDtos.ModerationPreview;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * What the moderation feature may do to stories. The story feature owns the status rules
 * (Story#hideForReview, #restore, #remove); moderation decides when.
 */
@Service
public class StoryModerationService {

    private final StoryRepository stories;

    public StoryModerationService(StoryRepository stories) {
        this.stories = stories;
    }

    /** @return true if this call hid it (it was visible). */
    @Transactional
    public boolean hideForReview(long storyId) {
        return stories.findById(storyId).map(Story::hideForReview).orElse(false);
    }

    @Transactional
    public void restore(long storyId) {
        stories.findById(storyId).ifPresent(Story::restore);
    }

    /** @return the author's user id (they get the strike). */
    @Transactional
    public long remove(long storyId) {
        Story story = stories.findById(storyId).orElseThrow(() -> ApiErrors.notFound("Story not found"));
        story.remove();
        return story.getUserId();
    }

    @Transactional
    public int removeAllBy(long userId) {
        return stories.removeAllByUserId(userId);
    }

    /** Any status; missing ids (deleted by the author) are simply absent. */
    @Transactional(readOnly = true)
    public Map<Long, ModerationPreview> previews(Collection<Long> storyIds) {
        if (storyIds.isEmpty()) return Map.of();
        return stories.findAllById(storyIds).stream().collect(Collectors.toMap(Story::getId, s -> new ModerationPreview(
                s.getId(), s.getUserId(), s.getSongId(), s.getBody(), s.getLyricQuote(), s.getStatus(), s.getCreatedAt())));
    }
}
