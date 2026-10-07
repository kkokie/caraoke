package com.caraoke.user;

import com.caraoke.media.MediaStorage;
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

    /** Profile photo changes go through PUT/DELETE /api/me/avatar (uploads), never a client-supplied URL. */
    public record UpdateProfileRequest(
            @Size(min = 1, max = 50) String displayName,
            @Size(max = 280) String bio) { }

    /** What anyone can see. Never exposes auth_uid. */
    public record PublicProfile(
            String handle,
            String displayName,
            String bio,
            String avatarUrl,
            Instant joinedAt) {

        static PublicProfile of(User u, MediaStorage media) {
            return new PublicProfile(u.getHandle(), u.getDisplayName(), u.getBio(), urlOf(u, media), u.getCreatedAt());
        }
    }

    public record HandleAvailability(String handle, boolean available, String reason) { }

    /** How other features (stories, replies) show who wrote something. */
    public record Author(String handle, String displayName, String avatarUrl) {

        static Author of(User u, MediaStorage media) {
            return new Author(u.getHandle(), u.getDisplayName(), urlOf(u, media));
        }
    }

    private static String urlOf(User u, MediaStorage media) {
        return u.getAvatarKey() == null ? null : media.publicUrl(u.getAvatarKey());
    }
}
