package com.caraoke.profile;

import com.caraoke.follow.FollowService;
import com.caraoke.user.UserDtos.Author;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** PUT/DELETE are idempotent, so the Follow button can fire them optimistically. */
@RestController
@RequestMapping("/api/profiles/{handle}")
public class ProfileFollowController {

    private final ProfileFollowService service;

    public ProfileFollowController(ProfileFollowService service) {
        this.service = service;
    }

    @PutMapping("/follow")
    public FollowService.State follow(Authentication auth, @PathVariable String handle) {
        return service.follow(auth.getName(), handle);
    }

    @DeleteMapping("/follow")
    public FollowService.State unfollow(Authentication auth, @PathVariable String handle) {
        return service.unfollow(auth.getName(), handle);
    }

    @GetMapping("/followers")
    public List<Author> followers(@PathVariable String handle) {
        return service.followers(handle);
    }

    @GetMapping("/following")
    public List<Author> following(@PathVariable String handle) {
        return service.following(handle);
    }
}
