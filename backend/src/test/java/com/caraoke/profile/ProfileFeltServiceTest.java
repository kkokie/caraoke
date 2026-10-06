package com.caraoke.profile;

import com.caraoke.resonance.ResonanceService;
import com.caraoke.story.AuthorStoriesService;
import com.caraoke.story.StoryDtos.StoryTile;
import com.caraoke.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileFeltServiceTest {

    @Mock UserService users;
    @Mock ResonanceService resonances;
    @Mock AuthorStoriesService authorStories;
    ProfileFeltService service;

    @BeforeEach
    void setUp() {
        service = new ProfileFeltService(users, resonances, authorStories);
    }

    @Test
    void myFeltTurnsIdsIntoTilesAndPassesTheCursorThrough() {
        when(users.findUserId("uid-ian")).thenReturn(Optional.of(5L));
        when(resonances.feltBy(5L, null, 30)).thenReturn(new ResonanceService.FeltPage(List.of(30L), "next"));
        when(authorStories.tilesFor(List.of(30L))).thenReturn(List.of(new StoryTile(30L, 7L, "Yellow", "Coldplay", "https://art", 2)));

        ProfileDtos.FeltTilePage page = service.myFelt("uid-ian", null, 0);

        assertThat(page.items()).extracting(StoryTile::storyId).containsExactly(30L);
        assertThat(page.nextCursor()).isEqualTo("next");
    }

    @Test
    void needsAProfile() {
        when(users.findUserId("uid-new")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.myFelt("uid-new", null, 30))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(403));
        verifyNoInteractions(resonances, authorStories);
    }
}
