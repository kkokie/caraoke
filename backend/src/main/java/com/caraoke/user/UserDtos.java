package com.caraoke.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** Request/response shapes for the user API. Records keep these immutable and boilerplate-free. */
public final class UserDtos {

    private UserDtos() { }

    public record CreateProfileRequest(
            @NotBlank String handle,
            @NotBlank @Size(max = 50) String displayName,
            @Size(max = 280) String bio) { }

    public record UpdateProfileRequest(
            @Size(min = 1, max = 50) String displayName,
            @Size(max = 280) String bio,
            @Size(max = 512) String avatarUrl) { }

    /** What anyone can see. Never exposes auth_uid. */
    public record PublicProfile(
            String handle,
            String displayName,
            String bio,
            String avatarUrl,
            Instant joinedAt) {

        static PublicProfile of(User u) {
            return new PublicProfile(u.getHandle(), u.getDisplayName(), u.getBio(), u.getAvatarUrl(), u.getCreatedAt());
        }
    }

    public record HandleAvailability(String handle, boolean available, String reason) { }
}
