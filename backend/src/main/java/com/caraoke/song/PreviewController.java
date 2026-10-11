package com.caraoke.song;

import com.caraoke.song.PreviewService.Preview;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
public class PreviewController {

    private final PreviewService previews;

    public PreviewController(PreviewService previews) {
        this.previews = previews;
    }

    /** Apple's 30-second clip to stream, with the attribution to show. 204 when there isn't one. */
    @GetMapping("/{id}/preview")
    public ResponseEntity<Preview> preview(@PathVariable long id) {
        return previews.preview(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
