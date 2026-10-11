package com.caraoke.page;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** How someone decorated their page. References songs and stories by id only. */
@Entity
@Table(name = "pages")
public class Page {

    static final List<PageBlock> DEFAULT_BLOCKS = List.of(PageBlock.values());

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PageTheme theme = PageTheme.PAPER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PageBackground background = PageBackground.PLAIN;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_pairing", nullable = false, length = 16)
    private PageType typePairing = PageType.CLASSIC;

    @Column(name = "page_song_id")
    private Long pageSongId;

    @Column(name = "pinned_story_id")
    private Long pinnedStoryId;

    @Column(nullable = false, length = 200)
    private String blocks = join(DEFAULT_BLOCKS);

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Page() { }

    Page(long userId) {
        this.userId = userId;
    }

    void decorate(PageTheme theme, PageBackground background, PageType type, List<PageBlock> blocks) {
        this.theme = theme;
        this.background = background;
        this.typePairing = type;
        this.blocks = join(blocks);
    }

    void setPageSongId(Long songId) { this.pageSongId = songId; }
    void setPinnedStoryId(Long storyId) { this.pinnedStoryId = storyId; }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getUserId() { return userId; }
    public PageTheme getTheme() { return theme; }
    public PageBackground getBackground() { return background; }
    public PageType getTypePairing() { return typePairing; }
    public Long getPageSongId() { return pageSongId; }
    public Long getPinnedStoryId() { return pinnedStoryId; }

    public List<PageBlock> getBlocks() {
        if (blocks == null || blocks.isBlank()) return List.of();
        return Arrays.stream(blocks.split(",")).map(PageBlock::valueOf).toList();
    }

    private static String join(List<PageBlock> blocks) {
        return blocks.stream().map(Enum::name).collect(Collectors.joining(","));
    }
}
