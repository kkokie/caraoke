package com.caraoke.moderation;

import com.caraoke.report.ReportReason;
import jakarta.persistence.*;

import java.time.Instant;

/** One upheld violation. Strikes add up along the StrikeLadder; voided ones don't count. */
@Entity
@Table(name = "strikes")
public class Strike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "story_id")
    private Long storyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportReason reason;

    @Column(name = "issued_by")
    private Long issuedBy;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected Strike() { }

    Strike(long userId, Long storyId, ReportReason reason, long issuedBy) {
        this.userId = userId;
        this.storyId = storyId;
        this.reason = reason;
        this.issuedBy = issuedBy;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getStoryId() { return storyId; }
    public ReportReason getReason() { return reason; }
    public Instant getVoidedAt() { return voidedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
