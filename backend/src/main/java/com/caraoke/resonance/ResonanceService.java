package com.caraoke.resonance;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Storage for "I felt this too". Deliberately knows nothing about stories:
 * the story feature checks visibility/ownership and then calls in here.
 * That keeps dependencies one-way (story -> resonance), with no cycle.
 */
@Service
public class ResonanceService {

    private final ResonanceRepository resonances;

    public ResonanceService(ResonanceRepository resonances) {
        this.resonances = resonances;
    }

    @Transactional
    public ResonanceSummary add(long userId, long storyId) {
        resonances.insertIfAbsent(userId, storyId);
        return new ResonanceSummary(resonances.countByIdStoryId(storyId), true);
    }

    @Transactional
    public ResonanceSummary remove(long userId, long storyId) {
        resonances.deleteOne(userId, storyId);
        return new ResonanceSummary(resonances.countByIdStoryId(storyId), false);
    }

    /** Count + "did the viewer resonate" for a page of stories, in two queries total. */
    @Transactional(readOnly = true)
    public Map<Long, ResonanceSummary> summaries(Collection<Long> storyIds, Long viewerId) {
        if (storyIds.isEmpty()) return Map.of();
        Map<Long, Long> counts = countsFor(storyIds);
        Set<Long> mine = viewerId == null ? Set.of() : new HashSet<>(resonances.findStoryIdsResonatedBy(viewerId, storyIds));
        return storyIds.stream().distinct().collect(Collectors.toMap(
                Function.identity(),
                id -> new ResonanceSummary(counts.getOrDefault(id, 0L), mine.contains(id))));
    }

    /** Total resonances across a set of stories (e.g. everything one person wrote). */
    @Transactional(readOnly = true)
    public long totalFor(Collection<Long> storyIds) {
        return storyIds.isEmpty() ? 0 : resonances.countByIdStoryIdIn(storyIds);
    }

    /** Newest first: the people who felt this story too. */
    @Transactional(readOnly = true)
    public List<Long> recentResonatorIds(long storyId, int limit) {
        return resonances.findByIdStoryIdOrderByCreatedAtDesc(storyId, Limit.of(limit)).stream()
                .map(r -> r.getId().getUserId())
                .toList();
    }

    /** One page of the stories a user felt (story ids, newest resonance first) + the next cursor. */
    public record FeltPage(List<Long> storyIds, String nextCursor) { }

    @Transactional(readOnly = true)
    public FeltPage feltBy(long userId, String rawCursor, int size) {
        FeltCursor cursor = FeltCursor.decode(rawCursor);
        List<Resonance> rows = cursor == null
                ? resonances.findByIdUserIdOrderByCreatedAtDescIdStoryIdDesc(userId, Limit.of(size + 1))
                : resonances.findFeltAfter(userId, cursor.createdAt(), cursor.storyId(), Limit.of(size + 1));
        boolean hasMore = rows.size() > size;
        List<Resonance> page = hasMore ? rows.subList(0, size) : rows;
        String next = hasMore ? FeltCursor.of(page.get(page.size() - 1)).encode() : null;
        return new FeltPage(page.stream().map(r -> r.getId().getStoryId()).toList(), next);
    }

    private Map<Long, Long> countsFor(Collection<Long> storyIds) {
        return resonances.countByStoryIds(storyIds).stream()
                .collect(Collectors.toMap(ResonanceRepository.StoryCount::getStoryId, ResonanceRepository.StoryCount::getTotal));
    }
}
