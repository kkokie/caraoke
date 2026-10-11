package com.caraoke.user;

/**
 * Where an account stands. WARNED changes nothing yet (the person is told); SUSPENDED can read
 * but not post until `suspendedUntil`; BANNED can only see their standing and delete the account.
 */
public enum Standing {
    GOOD, WARNED, SUSPENDED, BANNED;

    /** A suspension that has run out reads as a warning: the strikes are still on record. */
    public Standing effective(java.time.Instant suspendedUntil, java.time.Instant now) {
        if (this == SUSPENDED && (suspendedUntil == null || !suspendedUntil.isAfter(now))) return WARNED;
        return this;
    }
}
