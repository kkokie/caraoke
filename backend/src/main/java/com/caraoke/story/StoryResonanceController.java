package com.caraoke.story;

import com.caraoke.resonance.ResonanceSummary;
import com.caraoke.user.UserDtos.Author;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** PUT/DELETE are idempotent, so the app can fire them optimistically and retry safely. */
@RestController
@RequestMapping("/api/stories/{storyId}")
public class StoryResonanceController {

    private final StoryResonanceService service;

    public StoryResonanceController(StoryResonanceService service) {
        this.service = service;
    }

    @PutMapping("/resonance")
    public ResonanceSummary resonate(Authentication auth, @PathVariable long storyId) {
        return service.resonate(auth.getName(), storyId);
    }

    @DeleteMapping("/resonance")
    public ResonanceSummary unresonate(Authentication auth, @PathVariable long storyId) {
        return service.unresonate(auth.getName(), storyId);
    }

    @GetMapping("/resonators")
    public List<Author> resonators(@PathVariable long storyId) {
        return service.resonators(storyId);
    }
}
