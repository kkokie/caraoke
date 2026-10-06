package com.caraoke.profile;

import com.caraoke.profile.ProfileDtos.FeltTilePage;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileFeltController {

    private final ProfileFeltService service;

    public ProfileFeltController(ProfileFeltService service) {
        this.service = service;
    }

    /** Stories you felt, newest first. Only ever your own. */
    @GetMapping("/api/me/felt")
    public FeltTilePage myFelt(Authentication auth,
                               @RequestParam(name = "cursor", required = false) String cursor,
                               @RequestParam(name = "limit", defaultValue = "30") int limit) {
        return service.myFelt(auth.getName(), cursor, limit);
    }
}
