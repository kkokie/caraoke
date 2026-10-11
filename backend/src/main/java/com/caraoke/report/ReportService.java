package com.caraoke.report;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Storage for reports. Knows nothing about stories or users beyond ids: the safety feature checks
 * the target exists and decides what happens next. Reporting the same thing twice is a no-op.
 */
@Service
public class ReportService {

    static final int MAX_DETAILS = 500;

    private final ReportRepository reports;

    public ReportService(ReportRepository reports) {
        this.reports = reports;
    }

    /** @return how many different people currently have this target reported (including this one). */
    @Transactional
    public long file(long reporterId, ReportTarget type, long targetId, ReportReason reason, String details) {
        if (!reports.existsByReporterIdAndTargetTypeAndTargetIdAndResolvedAtIsNull(reporterId, type, targetId)) {
            reports.save(new Report(reporterId, type, targetId, reason, clean(details)));
        }
        return reports.countOpenReporters(type, targetId);
    }

    static String clean(String details) {
        if (details == null || details.isBlank()) return null;
        String d = details.strip();
        return d.length() > MAX_DETAILS ? d.substring(0, MAX_DETAILS) : d;
    }
}
