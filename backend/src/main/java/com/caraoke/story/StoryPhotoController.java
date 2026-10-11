package com.caraoke.story;

import com.caraoke.story.StoryDtos.StoryView;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/stories/{storyId}/photo")
public class StoryPhotoController {

    private final StoryPhotoService photos;

    public StoryPhotoController(StoryPhotoService photos) {
        this.photos = photos;
    }

    /** Multipart field "file". The app resizes first (~1600px) and sends its pixel size for layout. */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StoryView set(Authentication auth, @PathVariable long storyId, @RequestParam("file") MultipartFile file,
                         @RequestParam(required = false) Integer width,
                         @RequestParam(required = false) Integer height) throws IOException {
        return photos.set(auth.getName(), storyId, file.getBytes(), width, height);
    }

    @DeleteMapping
    public StoryView remove(Authentication auth, @PathVariable long storyId) {
        return photos.remove(auth.getName(), storyId);
    }
}
