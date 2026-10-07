package com.caraoke.profile;

import com.caraoke.profile.ProfileDtos.ProfileView;
import com.caraoke.story.StoryDtos.StoryTilePage;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles/{handle}")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    /** Header: public profile + stats + whether it's the viewer's own. */
    @GetMapping
    public ProfileView profile(Authentication auth, @PathVariable String handle) {
        return service.profile(auth.getName(), handle);
    }

    /** The grid, newest first. Pass nextCursor as ?before= for more. */
    @GetMapping("/stories")
    public StoryTilePage stories(@PathVariable String handle,
                                 @RequestParam(name = "before", required = false) Long before,
                                 @RequestParam(name = "limit", defaultValue = "30") int limit) {
        return service.stories(handle, before, limit);
    }
}
