package com.caraoke.story;

import jakarta.persistence.*;

import java.time.Instant;

/** A photo on a story: where it's stored, and its size for layout before it loads. */
@Entity
@Table(name = "story_media")
public class StoryMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "story_id", nullable = false)
    private Long storyId;

    @Column(nullable = false)
    private Short position;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    private Integer width;
    private Integer height;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected StoryMedia() { }

    StoryMedia(long storyId, int position, String storageKey, Integer width, Integer height) {
        this.storyId = storyId;
        this.position = (short) position;
        this.storageKey = storageKey;
        this.width = width;
        this.height = height;
    }

    public Long getStoryId() { return storyId; }
    public Short getPosition() { return position; }
    public String getStorageKey() { return storageKey; }
    public Integer getWidth() { return width; }
    public Integer getHeight() { return height; }
}
