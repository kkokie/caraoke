package com.caraoke.profile;

import com.caraoke.story.StoryDtos.AuthorStats;
import com.caraoke.user.UserDtos.PublicProfile;

/** Response shapes for the profile dashboard. */
public final class ProfileDtos {

    private ProfileDtos() { }

    /** Header of a profile page. `me` = the viewer is looking at their own profile. */
    public record ProfileView(PublicProfile user, AuthorStats stats, boolean me) { }
}
