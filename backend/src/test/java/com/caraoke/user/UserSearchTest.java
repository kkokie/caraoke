package com.caraoke.user;

import com.caraoke.media.MediaStorage;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Limit;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserSearchTest {

    private final UserRepository users = mock(UserRepository.class);
    private final com.caraoke.block.BlockService blocks = mock(com.caraoke.block.BlockService.class);
    private final UserService service = new UserService(users, mock(MediaStorage.class), blocks, mock(BannedAuthUidRepository.class));

    @Test
    void wildcardsAreEscapedNotStripped() {
        assertThat(UserService.likeEscape("maya_k")).isEqualTo("maya\\_k");
        assertThat(UserService.likeEscape("100%")).isEqualTo("100\\%");
        assertThat(UserService.likeEscape("a\\b")).isEqualTo("a\\\\b");
    }

    @Test
    void tooShortQueriesDontHitTheDatabase() {
        assertThat(service.searchPeople("u", "m")).isEmpty();
        assertThat(service.searchPeople("u", "@m")).isEmpty();
        assertThat(service.searchPeople("u", null)).isEmpty();
        verifyNoInteractions(users, blocks);
    }

    @Test
    void atSignIsIgnoredAndNameOnlyQueriesNeverMatchHandles() {
        when(users.search(anyString(), anyString(), any(Limit.class))).thenReturn(List.of());

        service.searchPeople("u", "@Maya_K");
        verify(users).search(eq("maya\\_k"), eq("maya\\_k"), any(Limit.class));

        service.searchPeople("u", "é!");
        verify(users).search(eq("#"), eq("é!"), any(Limit.class));
    }
}
