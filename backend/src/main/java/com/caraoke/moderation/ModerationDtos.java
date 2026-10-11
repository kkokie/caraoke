package com.caraoke.moderation;

import com.caraoke.report.ReportReason;
import com.caraoke.user.Standing;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/** Shapes for the admin API. Internal user ids never leave the server: people are @handles. */
public final class ModerationDtos {

    private ModerationDtos() { }

    public enum Kind { STORY, USER }

    /**
     * DISMISS: nothing wrong (a hidden story comes back). STRIKE: on a story, remove it and strike
     * its author; on a person, strike them. BAN: remove + ban straight away (severe cases).
     */
    public enum Action { DISMISS, STRIKE, BAN }

    /** `reason` is what goes on the strike; defaults to the first report's reason. */
    public record Decision(@NotNull Action action, ReportReason reason) { }

    public record Person(String handle, String displayName, Standing standing, Instant suspendedUntil, long activeStrikes) { }

    public record StoryPreview(long id, String songTitle, String songArtist, String body, String lyricQuote,
                               String status, Instant createdAt) { }

    public record ReportLine(ReportReason reason, String details, Instant createdAt) { }

    /** One reported thing. `story` is set for STORY items (null if the author already deleted it). */
    public record QueueItem(Kind kind, long firstReportId, long reporters, List<ReportReason> reasons,
                            Instant lastReportedAt, Person person, StoryPreview story, List<ReportLine> reports) { }

    /** Pass nextCursor back as ?after= ; null = you've reached the end. */
    public record QueuePage(List<QueueItem> items, Long nextCursor) { }

    /** What a decision did, for the app to confirm ("@sam: suspended until Oct 18"). */
    public record Outcome(Person person, int reportsClosed) { }

    public record StrikeLine(ReportReason reason, Long storyId, boolean voided, Instant createdAt) { }

    public record AccountReview(Person person, List<StrikeLine> strikes) { }
}
