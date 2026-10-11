package com.caraoke.report;

import jakarta.persistence.*;

import java.time.Instant;

/** One person flagging a story or a user for review. */
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 16)
    private ReportTarget targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportReason reason;

    @Column(length = 500)
    private String details;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Resolution resolution;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected Report() { }

    Report(long reporterId, ReportTarget targetType, long targetId, ReportReason reason, String details) {
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.details = details;
    }


    public Long getId() { return id; }
    public Long getReporterId() { return reporterId; }
    public ReportTarget getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public ReportReason getReason() { return reason; }
    public String getDetails() { return details; }
    public Instant getResolvedAt() { return resolvedAt; }
    public Resolution getResolution() { return resolution; }
    public Long getResolvedBy() { return resolvedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
