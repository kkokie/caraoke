package com.caraoke.report;

import com.caraoke.report.ReportRepository.QueueRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Storage for reports. Knows nothing about stories or users beyond ids: the safety feature checks
 * the target exists and decides what happens next. Reporting the same thing twice is a no-op.
 */
@Service
public class ReportService {

    static final int MAX_DETAILS = 500;

    private final ReportRepository reports;
    private final Clock clock;

    public ReportService(ReportRepository reports, Clock clock) {
        this.reports = reports;
        this.clock = clock;
    }

    /** @return how many different people currently have this target reported (including this one). */
    @Transactional
    public long file(long reporterId, ReportTarget type, long targetId, ReportReason reason, String details) {
        if (!reports.existsByReporterIdAndTargetTypeAndTargetIdAndResolvedAtIsNull(reporterId, type, targetId)) {
            reports.save(new Report(reporterId, type, targetId, reason, clean(details)));
        }
        return reports.countOpenReporters(type, targetId);
    }

    /** One reported thing waiting for a moderator. */
    public record OpenTarget(ReportTarget type, long targetId, long firstReportId, long reporters,
                             Instant lastReportedAt, List<ReportReason> reasons) { }

    /** A single report as a moderator reads it (who filed it stays private). */
    public record OpenReport(ReportReason reason, String details, Instant createdAt) { }

    /** Oldest first; pass the last item's firstReportId back as `after`. Fetches limit rows. */
    @Transactional(readOnly = true)
    public List<OpenTarget> queue(Long after, int limit) {
        return reports.findQueue(after == null ? 0L : after, limit).stream().map(ReportService::toTarget).toList();
    }

    @Transactional(readOnly = true)
    public List<OpenReport> openReports(ReportTarget type, long targetId) {
        return reports.findByTargetTypeAndTargetIdAndResolvedAtIsNullOrderByIdAsc(type, targetId).stream()
                .map(r -> new OpenReport(r.getReason(), r.getDetails(), r.getCreatedAt()))
                .toList();
    }

    /** @return how many open reports were closed (0 = nothing was open). */
    @Transactional
    public int resolve(ReportTarget type, long targetId, Resolution resolution, long moderatorId) {
        return reports.resolveOpen(type, targetId, resolution, moderatorId, clock.instant());
    }

    private static OpenTarget toTarget(QueueRow row) {
        List<ReportReason> reasons = Arrays.stream(row.getReasons().split(",")).map(ReportReason::valueOf).toList();
        return new OpenTarget(ReportTarget.valueOf(row.getTargetType()), row.getTargetId(), row.getFirstReportId(),
                row.getReporters(), Instant.ofEpochMilli(row.getLastReportedMillis()), reasons);
    }

    static String clean(String details) {
        if (details == null || details.isBlank()) return null;
        String d = details.strip();
        return d.length() > MAX_DETAILS ? d.substring(0, MAX_DETAILS) : d;
    }
}
