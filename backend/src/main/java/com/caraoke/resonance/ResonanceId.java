package com.caraoke.resonance;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/** Composite key: one resonance per (user, story). */
@Embeddable
public class ResonanceId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "story_id", nullable = false)
    private Long storyId;

    protected ResonanceId() { }  // JPA

    ResonanceId(long userId, long storyId) {
        this.userId = userId;
        this.storyId = storyId;
    }

    public Long getUserId() { return userId; }
    public Long getStoryId() { return storyId; }

    @Override
    public boolean equals(Object o) {
        return o instanceof ResonanceId other
                && Objects.equals(userId, other.userId)
                && Objects.equals(storyId, other.storyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, storyId);
    }
}
