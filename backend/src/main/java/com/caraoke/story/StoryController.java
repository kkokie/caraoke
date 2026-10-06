package com.caraoke.story;

import com.caraoke.story.StoryDtos.PostStoryRequest;
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
    public StoryView post(Authentication auth, @PathVariable long songId, @Valid @RequestBody PostStoryRequest req) {
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

    @DeleteMapping("/stories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable long id) {
        service.delete(auth.getName(), id);
    }
}
