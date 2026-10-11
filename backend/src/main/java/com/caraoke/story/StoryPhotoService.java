package com.caraoke.story;

import com.caraoke.common.ApiErrors;
import com.caraoke.media.ImageRules;
import com.caraoke.media.ImageRules.ImageType;
import com.caraoke.media.ImageScreening;
import com.caraoke.media.MediaCleanup;
import com.caraoke.media.MediaStorage;
import com.caraoke.story.StoryDtos.StoryView;
import com.caraoke.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * A photo on your story (one per story for now; the schema allows more). Every upload is
 * validated by its bytes and screened before anyone can see it. New accounts earn photos:
 * an account must be `app.photos.min-account-age-hours` old (bots rarely wait).
 */
@Service
public class StoryPhotoService {

    static final int MAX_SIDE = 10_000;          // sanity bound on client-reported pixel sizes

    private final StoryRepository stories;
    private final StoryMediaRepository media;
    private final MediaStorage storage;
    private final ImageScreening screening;
    private final UserService users;
    private final StoryViewAssembler views;
    private final Clock clock;
    private final Duration minAccountAge;

    public StoryPhotoService(StoryRepository stories, StoryMediaRepository media, MediaStorage storage,
                             ImageScreening screening, UserService users, StoryViewAssembler views, Clock clock,
                             @Value("${app.photos.min-account-age-hours:72}") long minAccountAgeHours) {
        this.stories = stories;
        this.media = media;
        this.storage = storage;
        this.screening = screening;
        this.users = users;
        this.views = views;
        this.clock = clock;
        this.minAccountAge = Duration.ofHours(minAccountAgeHours);
    }

    /** Sets (or replaces) the story's photo. The old file is deleted once this commits. */
    @Transactional
    public StoryView set(String authUid, long storyId, byte[] bytes, Integer width, Integer height) {
        long userId = requireProfile(authUid);
        Story story = requireOwnEditableStory(storyId, userId);
        requireEstablished(userId);
        ImageType type = ImageRules.validate(bytes);            // 400 for non-images / too big
        screening.check(bytes, type.contentType());             // 400 if the scan refuses it
        String key = "stories/" + storyId + "/" + UUID.randomUUID() + type.extension();
        storage.put(key, bytes, type.contentType());
        replaceCover(storyId, new StoryMedia(storyId, StoryPhotos.COVER, key, size(width), size(height)));
        return views.toView(story, userId);
    }

    @Transactional
    public StoryView remove(String authUid, long storyId) {
        long userId = requireProfile(authUid);
        Story story = requireOwnEditableStory(storyId, userId);
        replaceCover(storyId, null);
        return views.toView(story, userId);
    }

    private void replaceCover(long storyId, StoryMedia next) {
        media.findByStoryIdAndPosition(storyId, StoryPhotos.COVER).ifPresent(old -> {
            media.delete(old);
            media.flush();                                      // free the (story, position) slot first
            MediaCleanup.deleteAfterCommit(storage, old.getStorageKey());
        });
        if (next != null) media.save(next);
    }

    private void requireEstablished(long userId) {
        Instant joined = users.joinedAt(userId).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
        if (joined.plus(minAccountAge).isAfter(clock.instant())) {
            throw ApiErrors.forbidden("Photos unlock after your first few days on caraoke. Your words are enough for now.");
        }
    }

    private Story requireOwnEditableStory(long storyId, long userId) {
        Story story = stories.findById(storyId).orElseThrow(() -> ApiErrors.notFound("Story not found"));
        if (!story.isWrittenBy(userId)) throw ApiErrors.forbidden("You can only change your own stories.");
        if (story.isRemoved()) throw ApiErrors.forbidden("This story was removed for breaking the Community Guidelines.");
        return story;
    }

    private long requireProfile(String authUid) {
        return users.findUserId(authUid).orElseThrow(() -> ApiErrors.forbidden("Create your profile first."));
    }

    /** Pixel sizes come from the app (for layout only); nonsense is dropped, not trusted. */
    private static Integer size(Integer px) {
        return px == null || px <= 0 || px > MAX_SIDE ? null : px;
    }
}
