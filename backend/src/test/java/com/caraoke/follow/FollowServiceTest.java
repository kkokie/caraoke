package com.caraoke.follow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock FollowRepository repo;
    FollowService service;

    @BeforeEach
    void setUp() {
        service = new FollowService(repo);
    }

    @Test
    void followReturnsFreshFollowerCount() {
        when(repo.countByIdFolloweeId(6L)).thenReturn(41L);

        assertThat(service.follow(5L, 6L)).isEqualTo(new FollowService.State(true, 41));
        verify(repo).insertIfAbsent(5L, 6L);
    }

    @Test
    void cantFollowYourself() {
        assertThatThrownBy(() -> service.follow(5L, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(400));
        verifyNoInteractions(repo);
    }

    @Test
    void unfollowReturnsFreshFollowerCount() {
        when(repo.countByIdFolloweeId(6L)).thenReturn(40L);

        assertThat(service.unfollow(5L, 6L)).isEqualTo(new FollowService.State(false, 40));
        verify(repo).deleteOne(5L, 6L);
    }

    @Test
    void countsAreFollowersAndFollowing() {
        when(repo.countByIdFolloweeId(6L)).thenReturn(40L);
        when(repo.countByIdFollowerId(6L)).thenReturn(7L);

        assertThat(service.counts(6L)).isEqualTo(new FollowService.Counts(40, 7));
    }
}
