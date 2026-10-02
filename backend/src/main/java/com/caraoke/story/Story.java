package com.caraoke.story;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * A memory someone attached to a song. References the author and song by id only:
 * no JPA relations across feature boundaries.
 */
@Entity
@Table(name = "stories")
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "song_id", nullable = false)
    private Long songId;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(name = "moment_sec")
    private Integer momentSec;

    @Column(name = "year_of_memory")
    private Short yearOfMemory;          // SMALLINT in the DB

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StoryStatus status = StoryStatus.VISIBLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Story() { }  // JPA

    Story(long userId, long songId, String body, Integer momentSec, Short yearOfMemory) {
        this.userId = userId;
        this.songId = songId;
        this.body = body;
        this.momentSec = momentSec;
        this.yearOfMemory = yearOfMemory;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    boolean isWrittenBy(long userId) {
        return this.userId == userId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getSongId() { return songId; }
    public String getBody() { return body; }
    public Integer getMomentSec() { return momentSec; }
    public Short getYearOfMemory() { return yearOfMemory; }
    public StoryStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
