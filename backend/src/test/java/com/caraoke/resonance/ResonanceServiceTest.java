package com.caraoke.resonance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Limit;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResonanceServiceTest {

    /** Test stand-in for the Spring Data projection (record accessors match the interface getters). */
    record Count(Long getStoryId, Long getTotal) implements ResonanceRepository.StoryCount { }

    @Mock ResonanceRepository repo;
    ResonanceService service;

    @BeforeEach
    void setUp() {
        service = new ResonanceService(repo);
    }

    @Test
    void addIsIdempotentAndReturnsFreshCount() {
        when(repo.countByIdStoryId(30L)).thenReturn(4L);

        assertThat(service.add(5L, 30L)).isEqualTo(new ResonanceSummary(4, true));
        verify(repo).insertIfAbsent(5L, 30L);
    }

    @Test
    void removeReturnsFreshCount() {
        when(repo.countByIdStoryId(30L)).thenReturn(3L);

        assertThat(service.remove(5L, 30L)).isEqualTo(new ResonanceSummary(3, false));
        verify(repo).deleteOne(5L, 30L);
    }

    @Test
    void summariesCombineCountsAndViewerFlags() {
        List<Long> page = List.of(30L, 20L, 10L);
        when(repo.countByStoryIds(page)).thenReturn(List.of(new Count(30L, 2L), new Count(20L, 1L)));
        when(repo.findStoryIdsResonatedBy(5L, page)).thenReturn(List.of(20L));

        Map<Long, ResonanceSummary> result = service.summaries(page, 5L);

        assertThat(result).containsEntry(30L, new ResonanceSummary(2, false))
                          .containsEntry(20L, new ResonanceSummary(1, true))
                          .containsEntry(10L, new ResonanceSummary(0, false));   // nobody yet
    }

    @Test
    void anonymousViewerSkipsTheMineQuery() {
        when(repo.countByStoryIds(List.of(30L))).thenReturn(List.of());

        assertThat(service.summaries(List.of(30L), null)).containsEntry(30L, ResonanceSummary.NONE);
        verify(repo, never()).findStoryIdsResonatedBy(anyLong(), anyCollection());
    }

    @Test
    void emptyPageMakesNoQueries() {
        assertThat(service.summaries(List.of(), 5L)).isEmpty();
        verifyNoInteractions(repo);
    }

    // ---- stories a user felt ---------------------------------------------------

    @Test
    void feltFirstPageReturnsIdsAndACursorWhenMoreExist() {
        Instant t = Instant.parse("2026-10-05T20:00:00Z");
        when(repo.findByIdUserIdOrderByCreatedAtDescIdStoryIdDesc(eq(5L), any(Limit.class)))
                .thenReturn(List.of(felt(5L, 30L, t), felt(5L, 20L, t), felt(5L, 10L, t.minusSeconds(60))));

        ResonanceService.FeltPage page = service.feltBy(5L, null, 2);

        assertThat(page.storyIds()).containsExactly(30L, 20L);
        assertThat(page.nextCursor()).isEqualTo(new FeltCursor(t, 20L).encode());
    }

    @Test
    void feltNextPageUsesTheCursor() {
        Instant t = Instant.parse("2026-10-05T20:00:00Z");
        when(repo.findFeltAfter(eq(5L), eq(t), eq(20L), any(Limit.class)))
                .thenReturn(List.of(felt(5L, 10L, t.minusSeconds(60))));

        ResonanceService.FeltPage page = service.feltBy(5L, new FeltCursor(t, 20L).encode(), 2);

        assertThat(page.storyIds()).containsExactly(10L);
        assertThat(page.nextCursor()).isNull();
    }

    private static Resonance felt(long userId, long storyId, Instant at) {
        Resonance r = BeanUtils.instantiateClass(Resonance.class);
        ReflectionTestUtils.setField(r, "id", new ResonanceId(userId, storyId));
        ReflectionTestUtils.setField(r, "createdAt", at);
        return r;
    }
}
