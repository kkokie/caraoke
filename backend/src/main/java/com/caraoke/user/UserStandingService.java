package com.caraoke.user;

import com.caraoke.common.ApiErrors;
import com.caraoke.user.UserDtos.AccountStanding;
import com.caraoke.user.UserDtos.AccountSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Roles and standing. The user feature only stores where an account stands; the moderation
 * feature decides it (strikes, the ladder) and calls `setStanding`.
 */
@Service
public class UserStandingService {

    private final UserRepository users;
    private final BannedAuthUidRepository bannedUids;
    private final Clock clock;

    public UserStandingService(UserRepository users, BannedAuthUidRepository bannedUids, Clock clock) {
        this.users = users;
        this.bannedUids = bannedUids;
        this.clock = clock;
    }

    /** Your own standing (the app shows a notice when it isn't GOOD). */
    @Transactional(readOnly = true)
    public AccountStanding mine(String authUid) {
        return of(authUid).orElseThrow(() -> ApiErrors.notFound("Profile not created yet"));
    }

    /** Empty when the caller has no profile yet. Read on every API call by the standing gate. */
    @Transactional(readOnly = true)
    public Optional<AccountStanding> of(String authUid) {
        return users.findByAuthUid(authUid).map(this::standingOf);
    }

    /** The admin's user id, or 403. */
    @Transactional(readOnly = true)
    public long requireAdmin(String authUid) {
        return users.findByAuthUid(authUid)
                .filter(u -> u.getRole() == UserRole.ADMIN)
                .map(User::getId)
                .orElseThrow(() -> ApiErrors.forbidden("Moderators only."));
    }

    @Transactional
    public void setStanding(long userId, Standing standing, Instant suspendedUntil) {
        User u = users.findById(userId).orElseThrow(() -> ApiErrors.notFound("User not found"));
        u.changeStanding(standing, suspendedUntil);
        if (standing == Standing.BANNED) {
            bannedUids.save(new BannedAuthUid(u.getAuthUid(), clock.instant()));
        } else {
            bannedUids.deleteById(u.getAuthUid());
        }
    }

    /** Sign-up refuses a sign-in that was banned before (even after the account was deleted). */
    @Transactional(readOnly = true)
    public boolean wasBanned(String authUid) {
        return bannedUids.existsById(authUid);
    }

    /** Moderators see banned accounts too (public lookups hide them). */
    @Transactional(readOnly = true)
    public Optional<AccountSummary> findForModeration(String rawHandle) {
        return users.findByHandle(HandleRules.normalize(rawHandle)).map(this::summaryOf);
    }

    @Transactional(readOnly = true)
    public Map<Long, AccountSummary> summaries(Collection<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return users.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, this::summaryOf));
    }

    private AccountStanding standingOf(User u) {
        Standing effective = u.getStanding().effective(u.getSuspendedUntil(), clock.instant());
        Instant until = effective == Standing.SUSPENDED ? u.getSuspendedUntil() : null;
        return new AccountStanding(effective, until, u.getRole() == UserRole.ADMIN);
    }

    private AccountSummary summaryOf(User u) {
        AccountStanding s = standingOf(u);
        return new AccountSummary(u.getId(), u.getHandle(), u.getDisplayName(), u.getBio(), s.standing(), s.suspendedUntil());
    }
}
