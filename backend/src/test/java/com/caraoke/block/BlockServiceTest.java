package com.caraoke.block;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class BlockServiceTest {

    private final BlockRepository blocks = mock(BlockRepository.class);
    private final BlockService service = new BlockService(blocks);

    @Test
    void cantBlockYourself() {
        assertThatThrownBy(() -> service.block(5L, 5L)).isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(blocks);
    }

    @Test
    void signedOutViewersHideNoOne() {
        assertThat(service.hiddenFrom(null)).isEmpty();
        assertThat(service.eitherBlocked(null, 6L)).isFalse();
        verifyNoInteractions(blocks);
    }

    @Test
    void sqlHelpersNeverProduceEmptyLists() {
        assertThat(BlockService.notInList(Set.of())).containsExactly(-1L);
        assertThat(BlockService.notInList(Set.of(6L))).containsExactly(6L);
        assertThat(BlockService.arrayLiteral(Set.of())).isEqualTo("{}");
        assertThat(BlockService.arrayLiteral(Set.of(6L))).isEqualTo("{6}");
    }

    @Test
    void hiddenFromUsesBothDirections() {
        when(blocks.findHiddenFrom(5L)).thenReturn(List.of(6L, 7L));
        assertThat(service.hiddenFrom(5L)).containsExactlyInAnyOrder(6L, 7L);
    }
}
