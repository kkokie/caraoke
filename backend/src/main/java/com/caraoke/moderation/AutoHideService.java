package com.caraoke.moderation;

import com.caraoke.report.ReportTarget;
import com.caraoke.story.StoryModerationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Enough different people reported a story: hide it from every feed until a moderator decides.
 * Only stories; a reported person waits in the queue (hiding a whole account takes a human).
 */
@Service
public class AutoHideService {

    private final StoryModerationService stories;
    private final int threshold;

    public AutoHideService(StoryModerationService stories,
                           @Value("${app.moderation.auto-hide-reporters:3}") int threshold) {
        this.stories = stories;
        this.threshold = threshold;
    }

    /** @return true if this report tipped the story into hiding. */
    public boolean onReported(ReportTarget type, long targetId, long distinctReporters) {
        if (type != ReportTarget.STORY || distinctReporters < threshold) return false;
        return stories.hideForReview(targetId);
    }
}
