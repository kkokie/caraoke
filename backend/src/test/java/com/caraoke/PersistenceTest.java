package com.caraoke;

import com.caraoke.resonance.Resonance;
import com.caraoke.resonance.ResonanceRepository;
import com.caraoke.story.Story;
import com.caraoke.story.StoryRepository;
import com.caraoke.story.StoryStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against a real Postgres 16 in Docker. Booting this context alone proves:
 * Flyway migrations apply cleanly and every entity matches the schema (ddl-auto: validate).
 * The tests then cover the SQL that mocks can't: ON CONFLICT, GROUP BY projections, keyset paging.
 * Needs Docker running (GitHub Actions has it; locally, Docker Desktop).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PersistenceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired JdbcTemplate jdbc;
    @Autowired ResonanceRepository resonances;
    @Autowired StoryRepository stories;

    // ---- resonances ---------------------------------------------------------

    @Test
    void resonateIsIdempotent() {
        long song = song(), author = user("sam"), fan = user("ian");
        long s = story(author, song);

        assertThat(resonances.insertIfAbsent(fan, s)).isEqualTo(1);
        assertThat(resonances.insertIfAbsent(fan, s)).isEqualTo(0);   // second tap: no error, no double count
        assertThat(resonances.countByIdStoryId(s)).isEqualTo(1);

        assertThat(resonances.deleteOne(fan, s)).isEqualTo(1);
        assertThat(resonances.deleteOne(fan, s)).isEqualTo(0);
        assertThat(resonances.countByIdStoryId(s)).isZero();
    }

    @Test
    void batchCountsAndViewerFlags() {
        long song = song(), author = user("sam"), a = user("ann"), b = user("bob");
        long s1 = story(author, song), s2 = story(author, song), s3 = story(author, song);
        resonances.insertIfAbsent(a, s1);
        resonances.insertIfAbsent(b, s1);
        resonances.insertIfAbsent(a, s2);

        Map<Long, Long> counts = resonances.countByStoryIds(List.of(s1, s2, s3)).stream()
                .collect(Collectors.toMap(ResonanceRepository.StoryCount::getStoryId, ResonanceRepository.StoryCount::getTotal));

        assertThat(counts).containsEntry(s1, 2L).containsEntry(s2, 1L).doesNotContainKey(s3);
        assertThat(resonances.findStoryIdsResonatedBy(b, List.of(s1, s2, s3))).containsExactly(s1);
    }

    @Test
    void resonatorsAreNewestFirst() {
        long song = song(), author = user("sam"), early = user("early"), late = user("late");
        long s = story(author, song);
        jdbc.update("INSERT INTO resonances (user_id, story_id, created_at) VALUES (?, ?, now() - interval '1 hour')", early, s);
        jdbc.update("INSERT INTO resonances (user_id, story_id) VALUES (?, ?)", late, s);

        List<Long> userIds = resonances.findByIdStoryIdOrderByCreatedAtDesc(s, Limit.of(10)).stream()
                .map(Resonance::getId).map(id -> id.getUserId()).toList();

        assertThat(userIds).containsExactly(late, early);
    }

    // ---- story feed ---------------------------------------------------------

    @Test
    void feedIsKeysetPagedNewestFirstAndSkipsHidden() {
        long song = song(), author = user("sam");
        long oldest = story(author, song), hidden = story(author, song), middle = story(author, song), newest = story(author, song);
        jdbc.update("UPDATE stories SET status = 'HIDDEN' WHERE id = ?", hidden);

        List<Long> firstPage = ids(stories.findBySongIdAndStatusAndIdLessThanOrderByIdDesc(
                song, StoryStatus.VISIBLE, Long.MAX_VALUE, Limit.of(2)));
        List<Long> secondPage = ids(stories.findBySongIdAndStatusAndIdLessThanOrderByIdDesc(
                song, StoryStatus.VISIBLE, firstPage.get(1), Limit.of(2)));

        assertThat(firstPage).containsExactly(newest, middle);
        assertThat(secondPage).containsExactly(oldest);
    }

    // ---- fixtures (plain SQL so tests don't depend on entity constructors) ----

    private long user(String handle) {
        return jdbc.queryForObject(
                "INSERT INTO users (auth_uid, handle, display_name) VALUES (?, ?, ?) RETURNING id",
                Long.class, "uid-" + handle, handle, handle);
    }

    private long song() {
        return jdbc.queryForObject(
                "INSERT INTO songs (title, artist, apple_id) VALUES ('Mr. Brightside', 'The Killers', ?) RETURNING id",
                Long.class, String.valueOf(System.nanoTime()));
    }

    private long story(long userId, long songId) {
        return jdbc.queryForObject(
                "INSERT INTO stories (user_id, song_id, body) VALUES (?, ?, 'a memory') RETURNING id",
                Long.class, userId, songId);
    }

    private static List<Long> ids(List<Story> rows) {
        return rows.stream().map(Story::getId).toList();
    }
}
