package com.caraoke.page;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageRulesTest {

    @Test
    void blocksKeepTheirOrderAndAppearOnce() {
        assertThat(PageRules.blocks(List.of(PageBlock.RAISED_ME, PageBlock.PAGE_SONG, PageBlock.RAISED_ME)))
                .containsExactly(PageBlock.RAISED_ME, PageBlock.PAGE_SONG);
        assertThat(PageRules.blocks(List.of())).isEmpty();                       // a bare page is allowed
        assertThat(PageRules.blocks(null)).isEqualTo(Page.DEFAULT_BLOCKS);
    }

    @Test
    void upToFiveSongsThatRaisedYou() {
        assertThat(PageRules.raisedSongs(List.of(1L, 2L, 2L, 3L))).containsExactly(1L, 2L, 3L);
        assertThatThrownBy(() -> PageRules.raisedSongs(List.of(1L, 2L, 3L, 4L, 5L, 6L)))
                .isInstanceOf(ResponseStatusException.class);
    }
}
