package com.caraoke.user;

import com.caraoke.auth.AccountGate;
import com.caraoke.user.UserDtos.AccountStanding;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** Banned: nothing. Suspended: read, but no posting, editing, feeling, following or reporting. */
@Component
class StandingGate implements AccountGate {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MMM d", java.util.Locale.US).withZone(ZoneOffset.UTC);

    private final UserStandingService standing;

    StandingGate(UserStandingService standing) {
        this.standing = standing;
    }

    @Override
    public Optional<String> refusal(String authUid, boolean write) {
        return standing.of(authUid).flatMap(s -> refusal(s, write));
    }

    static Optional<String> refusal(AccountStanding s, boolean write) {
        return switch (s.standing()) {
            case BANNED -> Optional.of("This account was banned for breaking the Community Guidelines.");
            case SUSPENDED -> write
                    ? Optional.of("Your account is suspended until " + DAY.format(s.suspendedUntil())
                                  + " for breaking the Community Guidelines. You can still read.")
                    : Optional.empty();
            case GOOD, WARNED -> Optional.empty();
        };
    }
}
