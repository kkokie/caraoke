package com.caraoke.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetTypeAndTargetIdAndResolvedAtIsNull(
            long reporterId, ReportTarget targetType, long targetId);

    /** How many different people have an open report on this target (drives auto-hide). */
    @Query("""
            select count(distinct r.reporterId) from Report r
            where r.targetType = :type and r.targetId = :targetId and r.resolvedAt is null
            """)
    long countOpenReporters(@Param("type") ReportTarget type, @Param("targetId") long targetId);
}
