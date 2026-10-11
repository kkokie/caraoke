package com.caraoke.page;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Storage for page decoration, by internal user id. Knows nothing about songs or stories beyond
 * ids; the profile feature validates and assembles them.
 */
@Service
public class PageService {

    private final PageRepository pages;
    private final PageRaisedSongRepository raised;

    public PageService(PageRepository pages, PageRaisedSongRepository raised) {
        this.pages = pages;
        this.raised = raised;
    }

    /** A page nobody has decorated yet reads as the defaults. */
    @Transactional(readOnly = true)
    public PageSettings settings(long userId) {
        List<Long> raisedIds = raised.findByUserIdOrderByPositionAsc(userId).stream().map(PageRaisedSong::getSongId).toList();
        return pages.findById(userId)
                .map(p -> new PageSettings(p.getTheme(), p.getBackground(), p.getTypePairing(),
                        p.getPageSongId(), p.getPinnedStoryId(), p.getBlocks(), raisedIds))
                .orElseGet(() -> withRaised(PageSettings.defaults(), raisedIds));
    }

    /** Full replace. Missing look fields keep the defaults. */
    @Transactional
    public PageSettings save(long userId, PageSettings in) {
        Page page = pageOf(userId);
        page.decorate(
                PageRules.orDefault(in.theme(), PageTheme.PAPER),
                PageRules.orDefault(in.background(), PageBackground.PLAIN),
                PageRules.orDefault(in.type(), PageType.CLASSIC),
                PageRules.blocks(in.blocks()));
        page.setPageSongId(in.pageSongId());
        page.setPinnedStoryId(in.pinnedStoryId());
        pages.save(page);
        replaceRaised(userId, PageRules.raisedSongs(in.raisedSongIds()));
        return settings(userId);
    }

    /** Just the page song (onboarding step 2, or the quick picker). Null clears it. */
    @Transactional
    public void setPageSong(long userId, Long songId) {
        Page page = pageOf(userId);
        page.setPageSongId(songId);
        pages.save(page);
    }

    private Page pageOf(long userId) {
        return pages.findById(userId).orElseGet(() -> new Page(userId));
    }

    private void replaceRaised(long userId, List<Long> songIds) {
        raised.deleteAllForUser(userId);
        for (int i = 0; i < songIds.size(); i++) raised.save(new PageRaisedSong(userId, i, songIds.get(i)));
    }

    private static PageSettings withRaised(PageSettings s, List<Long> raisedIds) {
        return new PageSettings(s.theme(), s.background(), s.type(), s.pageSongId(), s.pinnedStoryId(), s.blocks(), raisedIds);
    }
}
