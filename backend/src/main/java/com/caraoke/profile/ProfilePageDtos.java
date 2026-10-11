package com.caraoke.profile;

import com.caraoke.page.PageBackground;
import com.caraoke.page.PageBlock;
import com.caraoke.page.PageTheme;
import com.caraoke.page.PageType;
import com.caraoke.song.SongDtos.SongSummary;
import com.caraoke.song.SongDtos.SongView;
import com.caraoke.story.StoryDtos.StoryTile;
import com.caraoke.story.StoryDtos.YearTile;

import java.util.List;

/** Shapes for someone's decorated page. */
public final class ProfilePageDtos {

    private ProfilePageDtos() { }

    /**
     * The page as drawn. `blocks` is the owner's chosen order; a block with nothing in it
     * (no page song, nothing pinned) is still listed, and the app skips it for visitors.
     */
    public record PageView(PageTheme theme, PageBackground background, PageType type, List<PageBlock> blocks,
                           SongView pageSong, StoryTile pinnedStory, List<YearTile> lifeInSongs,
                           List<SongSummary> raisedMe) { }

    /** Full replace of your page. Song ids are ours (resolve a search result first). */
    public record PageInput(PageTheme theme, PageBackground background, PageType type,
                            Long pageSongId, Long pinnedStoryId, List<PageBlock> blocks, List<Long> raisedSongIds) { }

    /** Just the page song (onboarding, quick pick). Null clears it. */
    public record PageSongInput(Long songId) { }
}
