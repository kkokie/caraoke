package com.caraoke.song;

import com.caraoke.song.SongDtos.ResolveRequest;
import com.caraoke.song.SongDtos.SearchResult;
import com.caraoke.song.SongDtos.SongView;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService service;

    public SongController(SongService service) {
        this.service = service;
    }

    /** Search the music catalog. Results aren't saved until someone opens one. */
    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam(name = "q", required = false) String query) {
        return service.search(query);
    }

    /** "Open" a search result: returns our song (creating it on first open). */
    @PostMapping("/resolve")
    public SongView resolve(@Valid @RequestBody ResolveRequest req) {
        return service.resolve(req.appleId());
    }

    @GetMapping("/{id}")
    public SongView get(@PathVariable long id) {
        return service.get(id);
    }
}
