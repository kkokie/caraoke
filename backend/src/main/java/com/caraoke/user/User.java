package com.caraoke.user;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auth_uid", nullable = false, unique = true, length = 128)
    private String authUid;

    @Column(nullable = false, unique = true, length = 20)
    private String handle;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(length = 280)
    private String bio;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() { }  // JPA

    public User(String authUid, String handle, String displayName, String bio) {
        this.authUid = authUid;
        this.handle = handle;
        this.displayName = displayName;
        this.bio = bio;
    }

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getAuthUid() { return authUid; }
    public String getHandle() { return handle; }
    public String getDisplayName() { return displayName; }
    public String getBio() { return bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public Instant getCreatedAt() { return createdAt; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setBio(String bio) { this.bio = bio; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
