package com.caraoke.story;

import com.caraoke.media.MediaCleanup;
import com.caraoke.media.MediaStorage;
import com.caraoke.story.StoryDtos.PhotoView;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/** Reads stories' photos for views and tiles, and cleans their files up when a story goes. */
@Component
class StoryPhotos {

    static final short COVER = 0;

    private final StoryMediaRepository media;
    private final MediaStorage storage;

    StoryPhotos(StoryMediaRepository media, MediaStorage storage) {
        this.media = media;
        this.storage = storage;
    }

    /** Each story's cover photo, if it has one (one query for the whole page). */
    Map<Long, PhotoView> covers(Collection<Long> storyIds) {
        if (storyIds.isEmpty()) return Map.of();
        return media.findByStoryIdInAndPosition(storyIds, COVER).stream()
                .collect(Collectors.toMap(StoryMedia::getStoryId, this::view));
    }

    PhotoView view(StoryMedia m) {
        return new PhotoView(storage.publicUrl(m.getStorageKey()), m.getWidth(), m.getHeight());
    }

    /** The rows cascade in the DB; the files are removed once the delete commits. */
    void deleteFilesOf(long storyId) {
        media.findByStoryId(storyId).forEach(m -> MediaCleanup.deleteAfterCommit(storage, m.getStorageKey()));
    }
}
