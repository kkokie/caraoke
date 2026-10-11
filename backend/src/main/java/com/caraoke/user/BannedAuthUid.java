package com.caraoke.user;

import jakarta.persistence.*;

import java.time.Instant;

/** A sign-in that was banned. Outlives account deletion so a ban can't be undone by re-signing up. */
@Entity
@Table(name = "banned_auth_uids")
class BannedAuthUid {

    @Id
    @Column(name = "auth_uid", length = 128)
    private String authUid;

    @Column(name = "banned_at", nullable = false)
    private Instant bannedAt;

    protected BannedAuthUid() { }

    BannedAuthUid(String authUid, Instant bannedAt) {
        this.authUid = authUid;
        this.bannedAt = bannedAt;
    }
}
