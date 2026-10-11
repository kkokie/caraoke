package com.caraoke.moderation;

import com.caraoke.report.ReportReason;
import com.caraoke.story.StoryModerationService;
import com.caraoke.user.Standing;
import com.caraoke.user.UserStandingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/** Issues strikes and keeps each account's standing in step with the ladder. */
@Service
class StrikeService {

    private final StrikeRepository strikes;
    private final UserStandingService standing;
    private final StoryModerationService stories;
    private final Clock clock;

    StrikeService(StrikeRepository strikes, UserStandingService standing, StoryModerationService stories, Clock clock) {
        this.strikes = strikes;
        this.standing = standing;
        this.stories = stories;
        this.clock = clock;
    }

    /** One more strike; the ladder decides the new standing. */
    @Transactional
    Standing strike(long userId, Long storyId, ReportReason reason, long moderatorId) {
        strikes.save(new Strike(userId, storyId, reason, moderatorId));
        return apply(userId, StrikeLadder.standingFor(strikes.countByUserIdAndVoidedAtIsNull(userId)));
    }

    /** Straight to a ban (severe cases), recorded as a strike so the history shows why. */
    @Transactional
    Standing ban(long userId, Long storyId, ReportReason reason, long moderatorId) {
        strikes.save(new Strike(userId, storyId, reason, moderatorId));
        return apply(userId, Standing.BANNED);
    }

    /** A moderator overturned it: strikes voided, standing back to GOOD. Removed stories stay removed. */
    @Transactional
    void reinstate(long userId) {
        strikes.voidAll(userId, clock.instant());
        standing.setStanding(userId, Standing.GOOD, null);
    }

    @Transactional(readOnly = true)
    long activeCount(long userId) {
        return strikes.countByUserIdAndVoidedAtIsNull(userId);
    }

    @Transactional(readOnly = true)
    List<Strike> recent(long userId) {
        return strikes.findTop20ByUserIdOrderByIdDesc(userId);
    }

    private Standing apply(long userId, Standing next) {
        Instant until = next == Standing.SUSPENDED ? clock.instant().plus(StrikeLadder.SUSPENSION) : null;
        standing.setStanding(userId, next, until);
        if (next == Standing.BANNED) stories.removeAllBy(userId);
        return next;
    }
}
