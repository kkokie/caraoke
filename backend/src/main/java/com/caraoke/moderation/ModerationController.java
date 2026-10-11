package com.caraoke.moderation;

import com.caraoke.moderation.ModerationDtos.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Admin-only (ModerationService checks the role on every call; non-admins get 403). */
@RestController
@RequestMapping("/api/admin")
public class ModerationController {

    private final ModerationService service;

    public ModerationController(ModerationService service) {
        this.service = service;
    }

    @GetMapping("/reports")
    public QueuePage queue(Authentication auth,
                           @RequestParam(required = false) Long after,
                           @RequestParam(defaultValue = "0") int size) {
        return service.queue(auth.getName(), after, size);
    }

    @PostMapping("/stories/{storyId}/decision")
    public Outcome decideStory(Authentication auth, @PathVariable long storyId, @Valid @RequestBody Decision d) {
        return service.decideStory(auth.getName(), storyId, d);
    }

    @PostMapping("/users/{handle}/decision")
    public Outcome decidePerson(Authentication auth, @PathVariable String handle, @Valid @RequestBody Decision d) {
        return service.decidePerson(auth.getName(), handle, d);
    }

    @GetMapping("/users/{handle}")
    public AccountReview review(Authentication auth, @PathVariable String handle) {
        return service.review(auth.getName(), handle);
    }

    @PostMapping("/users/{handle}/reinstate")
    public AccountReview reinstate(Authentication auth, @PathVariable String handle) {
        return service.reinstate(auth.getName(), handle);
    }
}
