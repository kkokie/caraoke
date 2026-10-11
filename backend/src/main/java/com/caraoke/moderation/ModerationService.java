package com.caraoke.moderation;

import com.caraoke.common.ApiErrors;
import com.caraoke.moderation.ModerationDtos.*;
import com.caraoke.report.ReportReason;
import com.caraoke.report.ReportService;
import com.caraoke.report.ReportService.OpenReport;
import com.caraoke.report.ReportTarget;
import com.caraoke.report.Resolution;
import com.caraoke.story.StoryModerationService;
import com.caraoke.user.UserDtos.AccountSummary;
import com.caraoke.user.UserStandingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Moderator decisions (moderation -> report, story, user, song). Every call checks the caller is an
 * admin first. Story decisions close the story's reports; person decisions close the person's.
 */
@Service
public class ModerationService {

    private final UserStandingService users;
    private final StoryModerationService stories;
    private final ReportService reports;
    private final StrikeService strikes;
    private final ModerationQueueService queue;

    ModerationService(UserStandingService users, StoryModerationService stories, ReportService reports,
                      StrikeService strikes, ModerationQueueService queue) {
        this.users = users;
        this.stories = stories;
        this.reports = reports;
        this.strikes = strikes;
        this.queue = queue;
    }

    @Transactional(readOnly = true)
    public QueuePage queue(String authUid, Long after, int size) {
        users.requireAdmin(authUid);
        return queue.page(after, size);
    }

    @Transactional
    public Outcome decideStory(String authUid, long storyId, Decision d) {
        long moderator = users.requireAdmin(authUid);
        ReportReason reason = reasonFor(d, ReportTarget.STORY, storyId);
        Long author = switch (d.action()) {
            case DISMISS -> { stories.restore(storyId); yield null; }
            case STRIKE -> strikeAuthor(storyId, reason, moderator, false);
            case BAN -> strikeAuthor(storyId, reason, moderator, true);
        };
        int closed = close(ReportTarget.STORY, storyId, d.action(), moderator);
        return new Outcome(author == null ? null : personById(author), closed);
    }

    @Transactional
    public Outcome decidePerson(String authUid, String handle, Decision d) {
        long moderator = users.requireAdmin(authUid);
        AccountSummary target = requirePerson(handle);
        if (target.id() == moderator && d.action() != Action.DISMISS) throw ApiErrors.badRequest("That's you.");
        ReportReason reason = reasonFor(d, ReportTarget.USER, target.id());
        switch (d.action()) {
            case DISMISS -> { }
            case STRIKE -> strikes.strike(target.id(), null, reason, moderator);
            case BAN -> strikes.ban(target.id(), null, reason, moderator);
        }
        int closed = close(ReportTarget.USER, target.id(), d.action(), moderator);
        return new Outcome(personById(target.id()), closed);
    }

    @Transactional(readOnly = true)
    public AccountReview review(String authUid, String handle) {
        users.requireAdmin(authUid);
        AccountSummary target = requirePerson(handle);
        List<StrikeLine> lines = strikes.recent(target.id()).stream()
                .map(s -> new StrikeLine(s.getReason(), s.getStoryId(), s.getVoidedAt() != null, s.getCreatedAt()))
                .toList();
        return new AccountReview(queue.person(target), lines);
    }

    /** Undo a mistake: strikes voided, back to GOOD. */
    @Transactional
    public AccountReview reinstate(String authUid, String handle) {
        users.requireAdmin(authUid);
        strikes.reinstate(requirePerson(handle).id());
        return review(authUid, handle);
    }

    // ---- helpers ---------------------------------------------------------------

    private Long strikeAuthor(long storyId, ReportReason reason, long moderator, boolean ban) {
        long author = stories.remove(storyId);
        if (ban) strikes.ban(author, storyId, reason, moderator);
        else strikes.strike(author, storyId, reason, moderator);
        return author;
    }

    private int close(ReportTarget type, long targetId, Action action, long moderator) {
        Resolution r = action == Action.DISMISS ? Resolution.DISMISSED : Resolution.ACTIONED;
        return reports.resolve(type, targetId, r, moderator);
    }

    private ReportReason reasonFor(Decision d, ReportTarget type, long targetId) {
        if (d.reason() != null) return d.reason();
        List<OpenReport> open = reports.openReports(type, targetId);
        return open.isEmpty() ? ReportReason.OTHER : open.get(0).reason();
    }

    private AccountSummary requirePerson(String handle) {
        return users.findForModeration(handle).orElseThrow(() -> ApiErrors.notFound("User not found"));
    }

    private Person personById(long userId) {
        return queue.person(users.summaries(Set.of(userId)).get(userId));
    }
}
