package com.caraoke.safety;

import com.caraoke.common.ApiErrors;
import com.caraoke.report.ReportService;
import com.caraoke.report.ReportTarget;
import com.caraoke.safety.SafetyDtos.ReportRequest;
import com.caraoke.story.StoryDtos.StoryView;
import com.caraoke.story.StoryService;
import com.caraoke.user.UserService;
import org.springframework.stereotype.Service;

/**
 * Reporting a story or a person. A composition feature (safety -> user, story, report): it checks
 * the thing exists and that you can see it, then files the report. What happens to reported
 * content (review, auto-hide) is the moderation feature's job.
 */
@Service
public class SafetyService {

    private final UserService users;
    private final StoryService stories;
    private final ReportService reports;

    public SafetyService(UserService users, StoryService stories, ReportService reports) {
        this.users = users;
        this.stories = stories;
        this.reports = reports;
    }

    public void report(String authUid, ReportRequest req) {
        long reporter = users.findUserId(authUid).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
        switch (req.kind()) {
            case STORY -> reportStory(authUid, reporter, req);
            case USER -> reportUser(reporter, req);
        }
    }

    private void reportStory(String authUid, long reporter, ReportRequest req) {
        if (req.storyId() == null) throw ApiErrors.badRequest("Which story?");
        StoryView story = stories.get(authUid, req.storyId());          // 404 if it doesn't exist for you
        if (story.mine()) throw ApiErrors.badRequest("That's your own story.");
        reports.file(reporter, ReportTarget.STORY, story.id(), req.reason(), req.details());
    }

    private void reportUser(long reporter, ReportRequest req) {
        long target = users.findUserIdByHandle(req.handle() == null ? "" : req.handle())
                .orElseThrow(() -> ApiErrors.notFound("User not found"));
        if (target == reporter) throw ApiErrors.badRequest("You can't report yourself.");
        reports.file(reporter, ReportTarget.USER, target, req.reason(), req.details());
    }
}
