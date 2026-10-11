package com.caraoke.profile;

import com.caraoke.story.StoryDtos.AuthorStats;
import com.caraoke.story.StoryDtos.StoryTile;
import com.caraoke.user.UserDtos.PublicProfile;

import java.util.List;

/** Response shapes for the profile dashboard. */
public final class ProfileDtos {

    private ProfileDtos() { }

    /** Follow counts plus whether the viewer follows this person. */
    public record SocialView(long followers, long following, boolean followedByMe) { }

    /**
     * Header of a profile page. `me` = the viewer is looking at their own profile.
     * `blockedByMe` = the viewer blocked this person (the app shows "Unblock" and no stories).
     */
    public record ProfileView(PublicProfile user, AuthorStats stats, SocialView social, boolean me, boolean blockedByMe) { }

    /** Someone you blocked, for the "Blocked accounts" list. */
    public record BlockedAccount(String handle, String displayName, String avatarUrl) { }

    /** Your private "Felt" grid. nextCursor is opaque: pass it back as ?cursor= for more. */
    public record FeltTilePage(List<StoryTile> items, String nextCursor) { }
}
