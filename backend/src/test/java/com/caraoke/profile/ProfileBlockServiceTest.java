package com.caraoke.profile;

import com.caraoke.block.BlockService;
import com.caraoke.follow.FollowService;
import com.caraoke.user.UserDtos.Author;
import com.caraoke.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileBlockServiceTest {

    @Mock UserService users;
    @Mock BlockService blocks;
    @Mock FollowService follows;
    ProfileBlockService service;

    @BeforeEach
    void setUp() {
        service = new ProfileBlockService(users, blocks, follows);
    }

    @Test
    void blockingEndsFollowsBothWays() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(5L));
        when(users.findUserIdByHandle("sam")).thenReturn(Optional.of(6L));

        service.block("uid-ian", "sam");

        InOrder order = inOrder(blocks, follows);
        order.verify(blocks).block(5L, 6L);
        verify(follows).unfollow(5L, 6L);
        verify(follows).unfollow(6L, 5L);
    }

    @Test
    void blockedAccountsListNewestFirst() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(5L));
        when(blocks.blockedBy(5L)).thenReturn(List.of(9L, 6L));
        when(users.findAuthors(List.of(9L, 6L))).thenReturn(Map.of(
                6L, new Author("sam", "Sam", null), 9L, new Author("jo", "Jo", null)));

        assertThat(service.blockedAccounts("uid-ian")).extracting(ProfileDtos.BlockedAccount::handle)
                .containsExactly("jo", "sam");
    }
}
