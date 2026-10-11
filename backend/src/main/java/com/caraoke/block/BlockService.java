package com.caraoke.block;

import com.caraoke.common.ApiErrors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Storage for blocks, by internal user id. A block works both ways: neither person sees the other's
 * stories, profile, or name in lists. Other features ask `hiddenFrom(viewer)` and filter.
 */
@Service
public class BlockService {

    private final BlockRepository blocks;

    public BlockService(BlockRepository blocks) {
        this.blocks = blocks;
    }

    @Transactional
    public void block(long blockerId, long blockedId) {
        if (blockerId == blockedId) throw ApiErrors.badRequest("You can't block yourself.");
        blocks.insertIfAbsent(blockerId, blockedId);
    }

    @Transactional
    public void unblock(long blockerId, long blockedId) {
        blocks.deleteOne(blockerId, blockedId);
    }

    /** Users this viewer must not see (either direction). Empty for signed-out viewers. */
    @Transactional(readOnly = true)
    public Set<Long> hiddenFrom(Long viewerId) {
        return viewerId == null ? Set.of() : Set.copyOf(blocks.findHiddenFrom(viewerId));
    }

    @Transactional(readOnly = true)
    public boolean eitherBlocked(Long a, long b) {
        return a != null && a != b && blocks.existsEitherWay(a, b);
    }

    @Transactional(readOnly = true)
    public boolean hasBlocked(long blockerId, long blockedId) {
        return blocks.existsById(new BlockId(blockerId, blockedId));
    }

    /** Ids this user blocked, newest first. */
    @Transactional(readOnly = true)
    public List<Long> blockedBy(long blockerId) {
        return blocks.findByIdBlockerIdOrderByCreatedAtDesc(blockerId).stream().map(b -> b.getId().getBlockedId()).toList();
    }

    /** For SQL `NOT IN`: never empty, so the query stays valid when nobody is hidden. */
    public static List<Long> notInList(Set<Long> hidden) {
        return hidden.isEmpty() ? List.of(-1L) : List.copyOf(hidden);
    }

    /** For Postgres `<> ALL(CAST(:x AS bigint[]))`. */
    public static String arrayLiteral(Set<Long> hidden) {
        return hidden.isEmpty() ? "{}" : hidden.stream().map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",", "{", "}"));
    }
}
