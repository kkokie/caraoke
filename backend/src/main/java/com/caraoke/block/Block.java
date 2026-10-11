package com.caraoke.block;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/** One user blocking another. Written with idempotent native SQL; this entity is for reads. */
@Entity
@Table(name = "blocks")
public class Block {

    @EmbeddedId
    private BlockId id;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected Block() { }

    public BlockId getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
}
