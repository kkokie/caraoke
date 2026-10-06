package com.caraoke.follow;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/** One user following another. Written with idempotent native SQL; this entity is for reads. */
@Entity
@Table(name = "follows")
public class Follow {

    @EmbeddedId
    private FollowId id;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;           // DB default now()

    protected Follow() { }  // JPA

    public FollowId getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
}
