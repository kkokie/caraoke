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

    @Column(nullable = false, columnDefinition = "text")
    private String body;               // up to StoryRules.MAX_BODY (10k) chars

    @Column(name = "moment_sec")
    private Integer momentSec;

    @Column(name = "year_of_memory")
    private Short yearOfMemory;          // SMALLINT in the DB

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StoryStatus status = StoryStatus.VISIBLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "edited_at")
    private Instant editedAt;

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

    /** Author edits keep the song and the author; everything else can change. */
    void edit(String body, Integer momentSec, Short yearOfMemory) {
        this.body = body;
        this.momentSec = momentSec;
        this.yearOfMemory = yearOfMemory;
        this.editedAt = Instant.now();
    }

    boolean isVisible() {
        return status == StoryStatus.VISIBLE;
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
    public Instant getEditedAt() { return editedAt; }
}
