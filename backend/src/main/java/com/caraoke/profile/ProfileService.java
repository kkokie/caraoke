package com.caraoke.profile;

import com.caraoke.common.ApiErrors;
import com.caraoke.profile.ProfileDtos.ProfileView;
import com.caraoke.story.AuthorStoriesService;
import com.caraoke.story.StoryDtos.StoryTilePage;
import com.caraoke.user.UserService;
import org.springframework.stereotype.Service;

/**
 * The profile dashboard is a composition feature: it sits ABOVE user and story and reads from both.
 * (user can't call story, since story already calls user; this keeps the dependency graph acyclic.)
 */
@Service
public class ProfileService {

    private final UserService users;
    private final AuthorStoriesService authorStories;

    public ProfileService(UserService users, AuthorStoriesService authorStories) {
        this.users = users;
        this.authorStories = authorStories;
    }

    public ProfileView profile(String viewerAuthUid, String handle) {
        long userId = requireUser(handle);
        boolean me = users.findUserId(viewerAuthUid).map(id -> id == userId).orElse(false);
        return new ProfileView(users.getByHandle(handle), authorStories.stats(userId), me);
    }

    public StoryTilePage stories(String handle, Long beforeId, int limit) {
        return authorStories.tiles(requireUser(handle), beforeId, limit);
    }

    private long requireUser(String handle) {
        return users.findUserIdByHandle(handle).orElseThrow(() -> ApiErrors.notFound("User not found"));
    }
}
