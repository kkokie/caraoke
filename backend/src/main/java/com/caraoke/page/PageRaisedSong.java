package com.caraoke.page;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/** One of "the songs that raised me", at a position (0 = first). */
@Entity
@Table(name = "page_raised_songs")
@IdClass(PageRaisedSong.Key.class)
public class PageRaisedSong {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "position")
    private Short position;

    @Column(name = "song_id", nullable = false)
    private Long songId;

    protected PageRaisedSong() { }

    PageRaisedSong(long userId, int position, long songId) {
        this.userId = userId;
        this.position = (short) position;
        this.songId = songId;
    }

    public Long getSongId() { return songId; }
    public Short getPosition() { return position; }

    public static class Key implements Serializable {
        private Long userId;
        private Short position;

        public Key() { }

        @Override public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(userId, k.userId) && Objects.equals(position, k.position);
        }

        @Override public int hashCode() { return Objects.hash(userId, position); }
    }
}
