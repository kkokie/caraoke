package com.caraoke.user;

import com.caraoke.common.ApiErrors;
import com.caraoke.user.UserDtos.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public PublicProfile getMe(String authUid) {
        // 404 here is meaningful: the app routes the user to onboarding (pick a handle)
        return users.findByAuthUid(authUid)
                .map(PublicProfile::of)
                .orElseThrow(() -> ApiErrors.notFound("Profile not created yet"));
    }

    @Transactional
    public PublicProfile createProfile(String authUid, CreateProfileRequest req) {
        if (users.findByAuthUid(authUid).isPresent()) {
            throw ApiErrors.conflict("Profile already exists");
        }
        String handle = HandleRules.normalize(req.handle());
        String problem = HandleRules.validate(handle);
        if (problem != null) {
            throw ApiErrors.badRequest(problem);
        }
        if (users.existsByHandle(handle)) {
            throw ApiErrors.conflict("That handle is taken");
        }
        try {
            User saved = users.saveAndFlush(new User(authUid, handle, req.displayName().trim(), blankToNull(req.bio())));
            return PublicProfile.of(saved);
        } catch (DataIntegrityViolationException race) {
            // Two people grabbed the same handle at the same moment; the unique index wins
            throw ApiErrors.conflict("That handle is taken");
        }
    }

    @Transactional
    public PublicProfile updateProfile(String authUid, UpdateProfileRequest req) {
        User u = users.findByAuthUid(authUid).orElseThrow(() -> ApiErrors.notFound("Profile not created yet"));
        if (req.displayName() != null) u.setDisplayName(req.displayName().trim());
        if (req.bio() != null) u.setBio(blankToNull(req.bio()));
        if (req.avatarUrl() != null) u.setAvatarUrl(blankToNull(req.avatarUrl()));
        return PublicProfile.of(u);  // dirty checking persists the changes on commit
    }

    /** In-app account deletion (App Store requirement). FKs cascade stories, resonances, etc. */
    @Transactional
    public void deleteAccount(String authUid) {
        users.findByAuthUid(authUid).ifPresent(users::delete);
    }

    @Transactional(readOnly = true)
    public PublicProfile getByHandle(String rawHandle) {
        return users.findByHandle(HandleRules.normalize(rawHandle))
                .map(PublicProfile::of)
                .orElseThrow(() -> ApiErrors.notFound("User not found"));
    }

    @Transactional(readOnly = true)
    public HandleAvailability checkHandle(String rawHandle) {
        String handle = HandleRules.normalize(rawHandle);
        String problem = HandleRules.validate(handle);
        if (problem != null) return new HandleAvailability(handle, false, problem);
        if (users.existsByHandle(handle)) return new HandleAvailability(handle, false, "That handle is taken");
        return new HandleAvailability(handle, true, null);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    // ---- Public API for other features ----------------------------------------
    // Other features use these instead of touching UserRepository or the User entity.

    /** Our internal user id for a signed-in caller, if they've created a profile. */
    @Transactional(readOnly = true)
    public Optional<Long> findUserId(String authUid) {
        return users.findByAuthUid(authUid).map(User::getId);
    }

    /** Our internal user id for a public @handle. */
    @Transactional(readOnly = true)
    public Optional<Long> findUserIdByHandle(String rawHandle) {
        return users.findByHandle(HandleRules.normalize(rawHandle)).map(User::getId);
    }

    /** Public author info for a batch of user ids (one query, no N+1). */
    @Transactional(readOnly = true)
    public Map<Long, Author> findAuthors(Collection<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return users.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Author::of));
    }
}
