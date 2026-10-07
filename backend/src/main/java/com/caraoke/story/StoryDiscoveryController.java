package com.caraoke.story;

import com.caraoke.story.DiscoverDtos.FoundStory;
import com.caraoke.story.DiscoverDtos.FoundStoryPage;
import com.caraoke.story.DiscoverDtos.SongWithCount;
import com.caraoke.story.DiscoverDtos.YearCount;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** The Discover tab: dig for a story, dig through the years, songs full of stories. */
@RestController
@RequestMapping("/api/discover")
public class StoryDiscoveryController {

    private final StoryDiscoveryService service;

    public StoryDiscoveryController(StoryDiscoveryService service) {
        this.service = service;
    }

    /** One random good story. `exclude` = ids already dug up this session. 204 when there's nothing left. */
    @GetMapping("/dig")
    public ResponseEntity<FoundStory> dig(Authentication auth,
                                          @RequestParam(name = "exclude", required = false) List<Long> exclude) {
        return service.dig(auth.getName(), exclude == null ? List.of() : exclude)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/years")
    public List<YearCount> years() {
        return service.years();
    }

    @GetMapping("/years/{year}")
    public FoundStoryPage byYear(Authentication auth, @PathVariable int year,
                                 @RequestParam(name = "before", required = false) Long before) {
        return service.byYear(auth.getName(), year, before);
    }

    @GetMapping("/songs")
    public List<SongWithCount> songs() {
        return service.songsFullOfStories();
    }
}
