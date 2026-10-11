package com.caraoke.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetTypeAndTargetIdAndResolvedAtIsNull(
            long reporterId, ReportTarget targetType, long targetId);

    /** How many different people have an open report on this target (drives auto-hide). */
    @Query("""
            select count(distinct r.reporterId) from Report r
            where r.targetType = :type and r.targetId = :targetId and r.resolvedAt is null
            """)
    long countOpenReporters(@Param("type") ReportTarget type, @Param("targetId") long targetId);

    /**
     * The moderation queue: one row per reported thing with open reports, oldest first.
     * Keyset on the first open report's id (`after`), so paging is stable while new reports arrive.
     */
    @Query(value = """
            SELECT target_type AS targetType, target_id AS targetId, MIN(id) AS firstReportId,
                   COUNT(DISTINCT reporter_id) AS reporters,
                   CAST(EXTRACT(EPOCH FROM MAX(created_at)) * 1000 AS BIGINT) AS lastReportedMillis,
                   STRING_AGG(DISTINCT reason, ',') AS reasons
            FROM reports
            WHERE resolved_at IS NULL
            GROUP BY target_type, target_id
            HAVING MIN(id) > :after
            ORDER BY MIN(id)
            LIMIT :limit
            """, nativeQuery = true)
    List<QueueRow> findQueue(@Param("after") long after, @Param("limit") int limit);

    interface QueueRow {
        String getTargetType();
        Long getTargetId();
        Long getFirstReportId();
        Long getReporters();
        Long getLastReportedMillis();     // epoch ms: plain numbers map reliably from native queries
        String getReasons();
    }

    List<Report> findByTargetTypeAndTargetIdAndResolvedAtIsNullOrderByIdAsc(ReportTarget type, long targetId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Report r set r.resolvedAt = :at, r.resolution = :resolution, r.resolvedBy = :by
            where r.targetType = :type and r.targetId = :targetId and r.resolvedAt is null
            """)
    int resolveOpen(@Param("type") ReportTarget type, @Param("targetId") long targetId,
                    @Param("resolution") Resolution resolution, @Param("by") long by, @Param("at") Instant at);
}
