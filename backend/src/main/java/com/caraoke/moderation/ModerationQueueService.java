package com.caraoke.moderation;

import com.caraoke.moderation.ModerationDtos.*;
import com.caraoke.report.ReportService;
import com.caraoke.report.ReportService.OpenTarget;
import com.caraoke.report.ReportTarget;
import com.caraoke.song.SongDtos.SongView;
import com.caraoke.song.SongService;
import com.caraoke.story.StoryDtos.ModerationPreview;
import com.caraoke.story.StoryModerationService;
import com.caraoke.user.UserDtos.AccountSummary;
import com.caraoke.user.UserStandingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The review queue: reported stories and people, oldest first, each with enough context to decide
 * without leaving the screen. A page is small (≤ 20), so per-item lookups are fine here.
 */
@Service
class ModerationQueueService {

    static final int DEFAULT_SIZE = 10;
    static final int MAX_SIZE = 20;

    private final ReportService reports;
    private final StoryModerationService stories;
    private final UserStandingService users;
    private final SongService songs;
    private final StrikeService strikes;

    ModerationQueueService(ReportService reports, StoryModerationService stories, UserStandingService users,
                           SongService songs, StrikeService strikes) {
        this.reports = reports;
        this.stories = stories;
        this.users = users;
        this.songs = songs;
        this.strikes = strikes;
    }

    @Transactional(readOnly = true)
    QueuePage page(Long after, int requestedSize) {
        int size = requestedSize <= 0 ? DEFAULT_SIZE : Math.min(requestedSize, MAX_SIZE);
        List<OpenTarget> rows = reports.queue(after, size + 1);
        boolean hasMore = rows.size() > size;
        List<OpenTarget> page = hasMore ? rows.subList(0, size) : rows;

        Map<Long, ModerationPreview> storyById = stories.previews(idsOf(page, ReportTarget.STORY));
        Map<Long, AccountSummary> people = users.summaries(peopleIds(page, storyById));
        List<QueueItem> items = page.stream().map(t -> item(t, storyById, people)).toList();
        return new QueuePage(items, hasMore ? page.get(page.size() - 1).firstReportId() : null);
    }

    Person person(AccountSummary a) {
        return new Person(a.handle(), a.displayName(), a.standing(), a.suspendedUntil(), strikes.activeCount(a.id()));
    }

    private QueueItem item(OpenTarget t, Map<Long, ModerationPreview> storyById, Map<Long, AccountSummary> people) {
        ModerationPreview story = t.type() == ReportTarget.STORY ? storyById.get(t.targetId()) : null;
        Long personId = t.type() == ReportTarget.USER ? Long.valueOf(t.targetId()) : story == null ? null : story.authorId();
        AccountSummary who = personId == null ? null : people.get(personId);
        return new QueueItem(
                t.type() == ReportTarget.STORY ? Kind.STORY : Kind.USER,
                t.firstReportId(), t.reporters(), t.reasons(), t.lastReportedAt(),
                who == null ? null : person(who),
                story == null ? null : preview(story),
                lines(t));
    }

    private StoryPreview preview(ModerationPreview s) {
        SongView song = songs.get(s.songId());
        return new StoryPreview(s.id(), song.title(), song.artist(), s.body(), s.lyricQuote(),
                s.status().name().toLowerCase(java.util.Locale.ROOT), s.createdAt());
    }

    private List<ReportLine> lines(OpenTarget t) {
        return reports.openReports(t.type(), t.targetId()).stream()
                .map(r -> new ReportLine(r.reason(), r.details(), r.createdAt()))
                .toList();
    }

    private static Set<Long> idsOf(List<OpenTarget> page, ReportTarget type) {
        Set<Long> ids = new HashSet<>();
        page.stream().filter(t -> t.type() == type).forEach(t -> ids.add(t.targetId()));
        return ids;
    }

    private static Set<Long> peopleIds(List<OpenTarget> page, Map<Long, ModerationPreview> storyById) {
        Set<Long> ids = idsOf(page, ReportTarget.USER);
        storyById.values().forEach(s -> ids.add(s.authorId()));
        return ids;
    }
}
