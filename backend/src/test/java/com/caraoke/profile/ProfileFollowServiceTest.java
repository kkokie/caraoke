package com.caraoke.profile;

import com.caraoke.follow.FollowService;
import com.caraoke.user.UserDtos.Author;
import com.caraoke.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileFollowServiceTest {

    @Mock UserService users;
    @Mock FollowService follows;
    ProfileFollowService service;

    @BeforeEach
    void setUp() {
        service = new ProfileFollowService(users, follows);
    }

    @Test
    void followByHandle() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(5L));
        when(users.findUserIdByHandle("sam")).thenReturn(Optional.of(6L));
        when(follows.follow(5L, 6L)).thenReturn(new FollowService.State(true, 41));

        assertThat(service.follow("uid-ian", "sam").followers()).isEqualTo(41);
    }

    @Test
    void followUnknownHandleIs404() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(5L));
        when(users.findUserIdByHandle("ghost")).thenReturn(Optional.empty());

        assertStatus(() -> service.follow("uid-ian", "ghost"), 404);
        verifyNoInteractions(follows);
    }

    @Test
    void followWithoutProfileIs403() {
        when(users.findUserId("uid-new")).thenReturn(Optional.empty());

        assertStatus(() -> service.follow("uid-new", "sam"), 403);
    }

    @Test
    void followersKeepNewestFirstOrder() {
        when(users.findUserIdByHandle("sam")).thenReturn(Optional.of(6L));
        when(follows.followerIds(6L, ProfileFollowService.LIST_LIMIT)).thenReturn(List.of(9L, 5L));
        when(users.findAuthors(List.of(9L, 5L))).thenReturn(Map.of(
                5L, new Author("ian", "Ian", null),
                9L, new Author("jo", "Jo", null)));

        assertThat(service.followers("sam")).extracting(Author::handle).containsExactly("jo", "ian");
    }

    private static void assertStatus(Runnable call, int status) {
        assertThatThrownBy(call::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(status));
    }
}
