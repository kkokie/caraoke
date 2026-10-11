package com.caraoke.page;

import java.util.List;

/**
 * Everything someone chose for their page, by id. The profile feature turns the ids into songs
 * and stories (and checks they exist and are yours) before saving.
 */
public record PageSettings(PageTheme theme, PageBackground background, PageType type,
                           Long pageSongId, Long pinnedStoryId, List<PageBlock> blocks, List<Long> raisedSongIds) {

    public static PageSettings defaults() {
        return new PageSettings(PageTheme.PAPER, PageBackground.PLAIN, PageType.CLASSIC, null, null,
                Page.DEFAULT_BLOCKS, List.of());
    }
}
