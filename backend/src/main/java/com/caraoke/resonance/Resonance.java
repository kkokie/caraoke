package com.caraoke.resonance;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * "I felt this too": a user recognizing themselves in a story.
 * Rows are written with idempotent native SQL (see ResonanceRepository); this entity is for reads.
 */
@Entity
@Table(name = "resonances")
public class Resonance {

    @EmbeddedId
    private ResonanceId id;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;           // DB default now()

    protected Resonance() { }  // JPA

    public ResonanceId getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
}
