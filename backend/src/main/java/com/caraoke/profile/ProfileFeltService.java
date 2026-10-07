package com.caraoke.profile;

import com.caraoke.common.ApiErrors;
import com.caraoke.profile.ProfileDtos.FeltTilePage;
import com.caraoke.resonance.ResonanceService;
import com.caraoke.resonance.ResonanceService.FeltPage;
import com.caraoke.story.AuthorStoriesService;
import com.caraoke.user.UserService;
import org.springframework.stereotype.Service;

/**
 * The stories you felt, as profile tiles. Private: there's deliberately no by-handle version,
 * only "me". (Like Instagram likes; easy to open up later if we decide to.)
 */
@Service
public class ProfileFeltService {

    static final int DEFAULT_PAGE = 30;
    static final int MAX_PAGE = 60;

    private final UserService users;
    private final ResonanceService resonances;
    private final AuthorStoriesService authorStories;

    public ProfileFeltService(UserService users, ResonanceService resonances, AuthorStoriesService authorStories) {
        this.users = users;
        this.resonances = resonances;
        this.authorStories = authorStories;
    }

    public FeltTilePage myFelt(String authUid, String cursor, int limit) {
        long me = users.findUserId(authUid).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
        FeltPage page = resonances.feltBy(me, cursor, clamp(limit));
        return new FeltTilePage(authorStories.tilesFor(page.storyIds()), page.nextCursor());
    }

    private static int clamp(int requested) {
        if (requested <= 0) return DEFAULT_PAGE;
        return Math.min(requested, MAX_PAGE);
    }
}
