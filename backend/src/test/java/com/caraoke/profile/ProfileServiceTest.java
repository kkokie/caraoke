package com.caraoke.profile;

import com.caraoke.profile.ProfileDtos.ProfileView;
import com.caraoke.story.AuthorStoriesService;
import com.caraoke.story.StoryDtos.AuthorStats;
import com.caraoke.user.UserDtos.PublicProfile;
import com.caraoke.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    private static final PublicProfile SAM =
            new PublicProfile("sam", "Sam", "90s kid", null, Instant.parse("2026-10-01T00:00:00Z"));

    @Mock UserService users;
    @Mock AuthorStoriesService authorStories;
    ProfileService service;

    @BeforeEach
    void setUp() {
        service = new ProfileService(users, authorStories);
    }

    @Test
    void someoneElsesProfile() {
        when(users.findUserIdByHandle("sam")).thenReturn(Optional.of(6L));
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(5L));
        when(users.getByHandle("sam")).thenReturn(SAM);
        when(authorStories.stats(6L)).thenReturn(new AuthorStats(3, 12));

        ProfileView view = service.profile("uid-ian", "sam");

        assertThat(view.me()).isFalse();
        assertThat(view.user().handle()).isEqualTo("sam");
        assertThat(view.stats()).isEqualTo(new AuthorStats(3, 12));
    }

    @Test
    void myOwnProfileIsFlagged() {
        when(users.findUserIdByHandle("sam")).thenReturn(Optional.of(6L));
        when(users.findUserId("uid-sam")).thenReturn(Optional.of(6L));
        when(users.getByHandle("sam")).thenReturn(SAM);
        when(authorStories.stats(6L)).thenReturn(new AuthorStats(0, 0));

        assertThat(service.profile("uid-sam", "sam").me()).isTrue();
    }

    @Test
    void unknownHandleIs404() {
        when(users.findUserIdByHandle("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.profile("uid-ian", "ghost"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(404));
        verifyNoInteractions(authorStories);
    }
}
