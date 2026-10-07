package com.caraoke.user;

import com.caraoke.media.MediaStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    @Mock UserRepository users;
    @Mock MediaStorage media;
    AvatarService service;
    User ian;

    @BeforeEach
    void setUp() {
        service = new AvatarService(users, media);
        ian = new User("uid-ian", "ian", "Ian", null);
        ReflectionTestUtils.setField(ian, "id", 5L);
    }

    @Test
    void uploadStoresUnderAFreshKeyAndReplacesTheOldPhoto() {
        ian.setAvatarKey("avatars/5/old.png");
        when(users.findByAuthUid("uid-ian")).thenReturn(Optional.of(ian));
        when(media.publicUrl(anyString())).thenAnswer(inv -> "/media/" + inv.getArgument(0));

        var profile = service.set("uid-ian", PNG);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(media).put(key.capture(), eq(PNG), eq("image/png"));
        assertThat(key.getValue()).startsWith("avatars/5/").endsWith(".png");
        assertThat(ian.getAvatarKey()).isEqualTo(key.getValue());
        assertThat(profile.avatarUrl()).isEqualTo("/media/" + key.getValue());
        verify(media).delete("avatars/5/old.png");              // no transaction in a unit test -> immediate
    }

    @Test
    void nonImagesAreRejectedBeforeAnythingIsStored() {
        when(users.findByAuthUid("uid-ian")).thenReturn(Optional.of(ian));

        assertThatThrownBy(() -> service.set("uid-ian", "hello".getBytes()))
                .isInstanceOf(ResponseStatusException.class);
        verify(media, never()).put(anyString(), any(), anyString());
        assertThat(ian.getAvatarKey()).isNull();
    }

    @Test
    void removeClearsTheKeyAndDeletesTheFile() {
        ian.setAvatarKey("avatars/5/old.png");
        when(users.findByAuthUid("uid-ian")).thenReturn(Optional.of(ian));

        var profile = service.remove("uid-ian");

        assertThat(profile.avatarUrl()).isNull();
        assertThat(ian.getAvatarKey()).isNull();
        verify(media).delete("avatars/5/old.png");
    }
}
