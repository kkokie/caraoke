package com.caraoke.profile;

import com.caraoke.profile.ProfilePageDtos.PageInput;
import com.caraoke.profile.ProfilePageDtos.PageSongInput;
import com.caraoke.profile.ProfilePageDtos.PageView;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProfilePageController {

    private final ProfilePageService service;

    public ProfilePageController(ProfilePageService service) {
        this.service = service;
    }

    /** Someone's decorated page: theme, background, type, page song, and their blocks in order. */
    @GetMapping("/profiles/{handle}/page")
    public PageView page(Authentication auth, @PathVariable String handle) {
        return service.page(auth.getName(), handle);
    }

    @PutMapping("/me/page")
    public PageView save(Authentication auth, @RequestBody PageInput in) {
        return service.save(auth.getName(), in);
    }

    @PutMapping("/me/page/song")
    public PageView setPageSong(Authentication auth, @RequestBody PageSongInput in) {
        return service.setPageSong(auth.getName(), in.songId());
    }
}
