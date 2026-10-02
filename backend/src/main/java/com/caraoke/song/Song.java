package com.caraoke.song;

import com.caraoke.song.catalog.CatalogTrack;
import jakarta.persistence.*;

import java.time.Instant;

/** Our canonical copy of a song. Stories hang off this id, never off a provider id. */
@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 12)
    private String isrc;                // filled later by MusicKit; iTunes Search doesn't expose it

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String artist;

    @Column(columnDefinition = "text")
    private String album;

    @Column(name = "album_art_url", columnDefinition = "text")
    private String albumArtUrl;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "apple_id", unique = true, columnDefinition = "text")
    private String appleId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Song() { }  // JPA

    static Song from(CatalogTrack t) {
        Song s = new Song();
        s.appleId = t.appleId();
        s.title = t.title();
        s.artist = t.artist();
        s.album = t.album();
        s.albumArtUrl = t.artworkUrl();
        s.durationSec = t.durationSec();
        return s;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getIsrc() { return isrc; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getAlbumArtUrl() { return albumArtUrl; }
    public Integer getDurationSec() { return durationSec; }
    public String getAppleId() { return appleId; }
}
