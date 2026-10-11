package com.caraoke.story;

import com.caraoke.media.ImageScreening;
import com.caraoke.media.MediaStorage;
import com.caraoke.resonance.ResonanceService;
import com.caraoke.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StoryPhotoServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-11T19:00:00Z");
    private static final long IAN = 5L, SAM = 9L;
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01};

    private final StoryRepository stories = mock(StoryRepository.class);
    private final StoryMediaRepository media = mock(StoryMediaRepository.class);
    private final MediaStorage storage = mock(MediaStorage.class);
    private final ImageScreening screening = mock(ImageScreening.class);
    private final UserService users = mock(UserService.class);
    private final StoryViewAssembler views =
            new StoryViewAssembler(users, mock(ResonanceService.class), mock(StoryPhotos.class));
    private final StoryPhotoService service = new StoryPhotoService(stories, media, storage, screening, users, views,
            Clock.fixed(NOW, ZoneOffset.UTC), 72);

    @BeforeEach
    void setUp() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(IAN));
        when(users.joinedAt(IAN)).thenReturn(Optional.of(NOW.minusSeconds(4 * 24 * 3600)));   // 4 days old
        when(stories.findById(20L)).thenReturn(Optional.of(story(20L, IAN)));
    }

    @Test
    void uploadIsScreenedStoredUnderTheStoryAndReplacesTheOldPhoto() {
        StoryMedia old = new StoryMedia(20L, 0, "stories/20/old.jpg", 800, 1000);
        when(media.findByStoryIdAndPosition(20L, (short) 0)).thenReturn(Optional.of(old));

        service.set("uid-ian", 20L, JPEG, 1600, 2000);

        verify(screening).check(JPEG, "image/jpeg");
        verify(storage).put(startsWith("stories/20/"), eq(JPEG), eq("image/jpeg"));
        verify(media).delete(old);
        verify(storage).delete("stories/20/old.jpg");             // no transaction in a unit test: deleted right away
        verify(media).save(argThat(m -> m.getWidth() == 1600 && m.getHeight() == 2000));
    }

    @Test
    void newAccountsWaitForPhotos() {
        when(users.joinedAt(IAN)).thenReturn(Optional.of(NOW.minusSeconds(3600)));          // an hour old

        assertThatThrownBy(() -> service.set("uid-ian", 20L, JPEG, null, null)).isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(storage);
    }

    @Test
    void onlyOnYourOwnStory() {
        when(stories.findById(21L)).thenReturn(Optional.of(story(21L, SAM)));

        assertThatThrownBy(() -> service.set("uid-ian", 21L, JPEG, null, null)).isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(storage);
    }

    @Test
    void aRefusedScanStoresNothing() {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST))
                .when(screening).check(any(), any());

        assertThatThrownBy(() -> service.set("uid-ian", 20L, JPEG, null, null)).isInstanceOf(ResponseStatusException.class);
        verify(storage, never()).put(any(), any(), any());
    }

    @Test
    void nonsenseSizesAreDropped() {
        when(media.findByStoryIdAndPosition(20L, (short) 0)).thenReturn(Optional.empty());

        service.set("uid-ian", 20L, JPEG, -3, 999_999);

        verify(media).save(argThat(m -> m.getWidth() == null && m.getHeight() == null));
    }

    private static Story story(long id, long authorId) {
        Story s = new Story(authorId, 7L, "words", null, null);
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }
}
