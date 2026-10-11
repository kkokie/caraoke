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

    @Column(name = "terms_version", length = 16)
    private String termsVersion;

    @Column(name = "terms_accepted_at")
    private Instant termsAcceptedAt;

    @Column(name = "avatar_key")
    private String avatarKey;          // object-storage key (never a URL); see MediaStorage

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

    /** Agreed to the Terms + Community Guidelines at this version. */
    void acceptTerms(String version, Instant at) {
        this.termsVersion = version;
        this.termsAcceptedAt = at;
    }

    public String getTermsVersion() { return termsVersion; }
    public Instant getTermsAcceptedAt() { return termsAcceptedAt; }

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
    public String getAvatarKey() { return avatarKey; }
    public Instant getCreatedAt() { return createdAt; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setBio(String bio) { this.bio = bio; }
    public void setAvatarKey(String avatarKey) { this.avatarKey = avatarKey; }
}
