package com.caraoke.story;

import com.caraoke.story.StoryDtos.ShareQuota;
import com.caraoke.story.StoryDtos.StoryInput;
import com.caraoke.story.StoryDtos.StoryPage;
import com.caraoke.story.StoryDtos.StoryView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class StoryController {

    private final StoryService service;

    public StoryController(StoryService service) {
        this.service = service;
    }

    @PostMapping("/songs/{songId}/stories")
    @ResponseStatus(HttpStatus.CREATED)
    public StoryView post(Authentication auth, @PathVariable long songId, @Valid @RequestBody StoryInput req) {
        return service.post(auth.getName(), songId, req);
    }

    /** Newest first. Pass the previous page's nextCursor as ?before= to keep scrolling. */
    @GetMapping("/songs/{songId}/stories")
    public StoryPage feed(Authentication auth,
                          @PathVariable long songId,
                          @RequestParam(name = "before", required = false) Long before,
                          @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return service.feed(auth.getName(), songId, before, limit);
    }

    /** One story a day: can I share now, and if not, when? */
    @GetMapping("/stories/quota")
    public ShareQuota quota(Authentication auth) {
        return service.quota(auth.getName());
    }

    @GetMapping("/stories/{id}")
    public StoryView get(Authentication auth, @PathVariable long id) {
        return service.get(auth.getName(), id);
    }

    /** Full replace of what the author wrote (body, moment, year). */
    @PutMapping("/stories/{id}")
    public StoryView edit(Authentication auth, @PathVariable long id, @Valid @RequestBody StoryInput req) {
        return service.edit(auth.getName(), id, req);
    }

    @DeleteMapping("/stories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable long id) {
        service.delete(auth.getName(), id);
    }
}
