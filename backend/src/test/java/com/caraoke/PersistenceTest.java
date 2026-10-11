package com.caraoke;

import com.caraoke.follow.Follow;
import com.caraoke.follow.FollowRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @Autowired FollowRepository follows;
    @Autowired com.caraoke.user.UserRepository users;
    @Autowired com.caraoke.block.BlockRepository blocks;
    @Autowired com.caraoke.report.ReportRepository reports;
    @Autowired com.caraoke.moderation.StrikeRepository strikes;

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

        List<Long> firstPage = ids(stories.findBySongIdAndStatusAndIdLessThanAndUserIdNotInOrderByIdDesc(
                song, StoryStatus.VISIBLE, Long.MAX_VALUE, List.of(-1L), Limit.of(2)));
        List<Long> secondPage = ids(stories.findBySongIdAndStatusAndIdLessThanAndUserIdNotInOrderByIdDesc(
                song, StoryStatus.VISIBLE, firstPage.get(1), List.of(-1L), Limit.of(2)));

        assertThat(firstPage).containsExactly(newest, middle);
        assertThat(secondPage).containsExactly(oldest);
    }

    // ---- profile ------------------------------------------------------------

    @Test
    void authorGridIsKeysetPagedAndStatsCountOnlyVisible() {
        long song = song(), ian = user("ian"), sam = user("sam"), fan = user("fan");
        long first = story(ian, song), hidden = story(ian, song), second = story(ian, song);
        story(sam, song);                                          // someone else's: never on ian's grid
        jdbc.update("UPDATE stories SET status = 'HIDDEN' WHERE id = ?", hidden);
        resonances.insertIfAbsent(fan, first);
        resonances.insertIfAbsent(fan, second);
        resonances.insertIfAbsent(sam, second);

        List<Long> page1 = ids(stories.findByUserIdAndStatusAndIdLessThanOrderByIdDesc(
                ian, StoryStatus.VISIBLE, Long.MAX_VALUE, Limit.of(1)));
        List<Long> page2 = ids(stories.findByUserIdAndStatusAndIdLessThanOrderByIdDesc(
                ian, StoryStatus.VISIBLE, page1.get(0), Limit.of(5)));
        List<Long> visible = stories.findIdsByUserIdAndStatus(ian, StoryStatus.VISIBLE);

        assertThat(page1).containsExactly(second);
        assertThat(page2).containsExactly(first);
        assertThat(visible).containsExactlyInAnyOrder(first, second);
        assertThat(resonances.countByIdStoryIdIn(visible)).isEqualTo(3);
    }

    @Test
    void editedAtColumnRoundTrips() {
        long s = story(user("ian"), song());
        jdbc.update("UPDATE stories SET edited_at = now() WHERE id = ?", s);

        assertThat(stories.findById(s).orElseThrow().getEditedAt()).isNotNull();
    }

    @Test
    void storiesCanBeBlogLengthButNotUnbounded() {
        long ian = user("ian"), song = song();
        long longOne = jdbc.queryForObject(
                "INSERT INTO stories (user_id, song_id, body) VALUES (?, ?, ?) RETURNING id",
                Long.class, ian, song, "x".repeat(10_000));

        assertThat(stories.findById(longOne).orElseThrow().getBody()).hasSize(10_000);
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO stories (user_id, song_id, body) VALUES (?, ?, ?)", ian, song, "x".repeat(10_001)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void lyricLineAndPaperRoundTripAndUnknownPapersAreRejected() {
        long s = story(user("ian"), song());
        jdbc.update("UPDATE stories SET lyric_quote = ?, paper = 'SAGE' WHERE id = ?", "a line I felt", s);

        Story found = stories.findById(s).orElseThrow();
        assertThat(found.getLyricQuote()).isEqualTo("a line I felt");
        assertThat(found.getPaper()).isEqualTo(com.caraoke.story.Paper.SAGE);
        assertThatThrownBy(() -> jdbc.update("UPDATE stories SET paper = 'NEON' WHERE id = ?", s))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void latestStoryPerAuthorIsFoundForTheDailyLimit() {
        long ian = user("ian"), song = song();
        long older = story(ian, song);
        long newer = story(ian, song);
        jdbc.update("UPDATE stories SET created_at = now() - interval '2 days' WHERE id = ?", older);

        assertThat(stories.findFirstByUserIdOrderByCreatedAtDesc(ian)).get()
                .extracting(Story::getId).isEqualTo(newer);
        assertThat(stories.findFirstByUserIdOrderByCreatedAtDesc(user("sam"))).isEmpty();
    }

    // ---- follows ------------------------------------------------------------

    @Test
    void followGraphIsIdempotentAndCountsBothWays() {
        long ian = user("ian"), sam = user("sam"), jo = user("joe");

        assertThat(follows.insertIfAbsent(ian, sam)).isEqualTo(1);
        assertThat(follows.insertIfAbsent(ian, sam)).isEqualTo(0);       // double tap
        jdbc.update("INSERT INTO follows (follower_id, followee_id, created_at) VALUES (?, ?, now() - interval '1 hour')", jo, sam);
        follows.insertIfAbsent(sam, jo);

        assertThat(follows.countByIdFolloweeId(sam)).isEqualTo(2);       // ian + jo follow sam
        assertThat(follows.countByIdFollowerId(sam)).isEqualTo(1);       // sam follows jo
        assertThat(follows.findByIdFolloweeIdOrderByCreatedAtDesc(sam, Limit.of(10)).stream()
                .map(Follow::getId).map(id -> id.getFollowerId()).toList())
                .containsExactly(ian, jo);                               // newest first
        assertThat(follows.deleteOne(ian, sam)).isEqualTo(1);
        assertThat(follows.countByIdFolloweeId(sam)).isEqualTo(1);
    }

    @Test
    void cantFollowYourselfEvenAtTheDatabase() {
        long ian = user("ian");
        assertThatThrownBy(() -> jdbc.update("INSERT INTO follows (follower_id, followee_id) VALUES (?, ?)", ian, ian))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---- stories I felt (keyset on created_at + story_id) ----------------------

    @Test
    void feltPagesNewestFirstAndBreaksTimestampTies() {
        long song = song(), author = user("sam"), me = user("ian");
        long a = story(author, song), b = story(author, song), c = story(author, song);
        // b and c share a timestamp: only story_id can order them, which is why the cursor carries it
        jdbc.update("INSERT INTO resonances (user_id, story_id, created_at) VALUES (?, ?, now() - interval '2 hour')", me, a);
        jdbc.update("INSERT INTO resonances (user_id, story_id, created_at) VALUES (?, ?, now() - interval '1 hour')", me, b);
        jdbc.update("INSERT INTO resonances (user_id, story_id, created_at) VALUES (?, ?, now() - interval '1 hour')", me, c);

        List<Resonance> page1 = resonances.findByIdUserIdOrderByCreatedAtDescIdStoryIdDesc(me, Limit.of(2));
        Resonance last = page1.get(1);
        List<Resonance> page2 = resonances.findFeltAfter(me, last.getCreatedAt(), last.getId().getStoryId(), Limit.of(5));

        assertThat(page1.stream().map(r -> r.getId().getStoryId()).toList()).containsExactly(c, b);   // tie -> higher id first
        assertThat(page2.stream().map(r -> r.getId().getStoryId()).toList()).containsExactly(a);
    }

    // ---- discover -----------------------------------------------------------

    private static final String LONG = "x".repeat(100);   // over the 80-char dig floor

    @Test
    void digFindsOnlyRealStoriesNotYoursAndNotAlreadySeen() {
        long ian = user("ian"), sam = user("sam"), song = song();
        storyWith(sam, song, "too short", null);                       // under the quality floor
        storyWith(ian, song, LONG, null);                              // the viewer's own
        long seen = storyWith(sam, song, LONG, null);
        long fresh = storyWith(sam, song, LONG, null);

        assertThat(stories.findRandomForDig(ian, 80, "{" + seen + "}", "{}")).get().extracting(Story::getId).isEqualTo(fresh);
        assertThat(stories.findRandomForDig(ian, 80, "{" + seen + "," + fresh + "}", "{}")).isEmpty();
        assertThat(stories.findRandomForDig(-1L, 80, "{}", "{}")).isPresent();       // signed-out viewer id
        assertThat(stories.findRandomForDig(ian, 80, "{" + seen + "}", "{" + sam + "}")).isEmpty();   // blocked author
    }

    @Test
    void yearsAndSongsFullOfStoriesCountOnlyVisible() {
        long ian = user("ian"), a = song(), b = song();
        storyWith(ian, a, LONG, 2009);
        storyWith(ian, a, LONG, 2009);
        storyWith(ian, b, LONG, 1998);
        long hidden = storyWith(ian, b, LONG, 1998);
        jdbc.update("UPDATE stories SET status = 'HIDDEN' WHERE id = ?", hidden);

        assertThat(stories.countByYear()).extracting(StoryRepository.YearCount::getYear, StoryRepository.YearCount::getStories)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(2009, 2L), org.assertj.core.groups.Tuple.tuple(1998, 1L));
        assertThat(stories.topSongs(10)).extracting(StoryRepository.SongCount::getSongId).containsExactly(a, b);
        assertThat(ids(stories.findByYearOfMemoryAndStatusAndIdLessThanAndUserIdNotInOrderByIdDesc(
                (short) 1998, StoryStatus.VISIBLE, Long.MAX_VALUE, List.of(-1L), Limit.of(10)))).hasSize(1);
        assertThat(ids(stories.findByYearOfMemoryAndStatusAndIdLessThanAndUserIdNotInOrderByIdDesc(
                (short) 1998, StoryStatus.VISIBLE, Long.MAX_VALUE, List.of(ian), Limit.of(10)))).isEmpty();
    }

    @Test
    void peopleSearchMatchesHandlePrefixOrNameAndTreatsWildcardsLiterally() {
        long maya = user("maya_k");
        jdbc.update("UPDATE users SET display_name = 'Maya Kim' WHERE id = ?", maya);
        user("mayor");
        user("sam");

        assertThat(handles(users.search("maya\\_", "maya\\_", Limit.of(20)))).containsExactly("maya_k");
        assertThat(handles(users.search("may", "may", Limit.of(20)))).containsExactly("maya_k", "mayor");
        assertThat(handles(users.search("#", "kim", Limit.of(20)))).containsExactly("maya_k");   // name only
        assertThat(handles(users.search("\\%", "\\%", Limit.of(20)))).isEmpty();              // "%" is not a wildcard
    }

    private static List<String> handles(List<com.caraoke.user.User> rows) {
        return rows.stream().map(com.caraoke.user.User::getHandle).toList();
    }

    private long storyWith(long userId, long songId, String body, Integer year) {
        return jdbc.queryForObject(
                "INSERT INTO stories (user_id, song_id, body, year_of_memory) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, userId, songId, body, year);
    }

    // ---- story photos -----------------------------------------------------------

    @Autowired com.caraoke.story.StoryMediaRepository storyMedia;

    @Test
    void coversComeBackForAPageInOneQueryAndGoWithTheirStory() {
        long sam = user("sam"), song = song();
        long withPhoto = story(sam, song), without = story(sam, song);
        jdbc.update("INSERT INTO story_media (story_id, position, storage_key, width, height) VALUES (?, 0, 'stories/a.jpg', 1600, 2000)", withPhoto);
        jdbc.update("INSERT INTO story_media (story_id, position, storage_key) VALUES (?, 1, 'stories/b.jpg')", withPhoto);   // not the cover

        assertThat(storyMedia.findByStoryIdInAndPosition(List.of(withPhoto, without), (short) 0))
                .extracting(m -> m.getStorageKey()).containsExactly("stories/a.jpg");

        jdbc.update("DELETE FROM stories WHERE id = ?", withPhoto);
        assertThat(storyMedia.findByStoryId(withPhoto)).isEmpty();
    }

    @Test
    void oneCoverPerStory() {
        long sam = user("sam"), song = song();
        long s = story(sam, song);
        jdbc.update("INSERT INTO story_media (story_id, position, storage_key) VALUES (?, 0, 'stories/a.jpg')", s);

        assertThatThrownBy(() -> jdbc.update("INSERT INTO story_media (story_id, position, storage_key) VALUES (?, 0, 'stories/b.jpg')", s))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---- moments --------------------------------------------------------------

    @Test
    void momentsGroupIntoFiveSecondSlots() {
        long sam = user("sam"), song = song();
        long a = story(sam, song), b = story(sam, song), c = story(sam, song);
        story(sam, song);                                                        // no moment
        jdbc.update("UPDATE stories SET moment_sec = 131 WHERE id = ?", a);
        jdbc.update("UPDATE stories SET moment_sec = 134 WHERE id = ?", b);
        jdbc.update("UPDATE stories SET moment_sec = 40 WHERE id = ?", c);

        assertThat(stories.countMoments(song))
                .extracting(StoryRepository.MomentCount::getSec, StoryRepository.MomentCount::getStories)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(40, 1L), org.assertj.core.groups.Tuple.tuple(130, 2L));
    }

    // ---- safety -------------------------------------------------------------

    @Test
    void blocksHideBothWaysAndAreIdempotent() {
        long ian = user("ian"), sam = user("sam"), joe = user("joe");
        blocks.insertIfAbsent(ian, sam);
        blocks.insertIfAbsent(ian, sam);                 // double tap
        blocks.insertIfAbsent(joe, ian);                 // joe blocked ian

        assertThat(blocks.findHiddenFrom(ian)).containsExactlyInAnyOrder(sam, joe);
        assertThat(blocks.findHiddenFrom(sam)).containsExactly(ian);
        assertThat(blocks.existsEitherWay(sam, ian)).isTrue();
        assertThat(blocks.existsEitherWay(sam, joe)).isFalse();
        assertThatThrownBy(() -> blocks.insertIfAbsent(ian, ian)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sameWordsSharedRecentlyAreFoundIgnoringCaseAndSpacing() {
        long sam = user("sam"), song = song();
        long s = storyWith(sam, song, "Dad's truck,   windows DOWN", null);

        assertThat(stories.existsSameTextSince("dad's truck, windows down", java.time.Instant.now().minusSeconds(3600), -1L)).isTrue();
        assertThat(stories.existsSameTextSince("dad's truck, windows down", java.time.Instant.now().minusSeconds(3600), s)).isFalse();
        assertThat(stories.existsSameTextSince("something else", java.time.Instant.now().minusSeconds(3600), -1L)).isFalse();
    }

    @Test
    void reportersAreCountedOncePerPerson() {
        long ian = user("ian"), sam = user("sam"), joe = user("joe"), song = song();
        long target = story(joe, song);
        jdbc.update("INSERT INTO reports (reporter_id, target_type, target_id, reason) VALUES (?, 'STORY', ?, 'SPAM')", ian, target);
        jdbc.update("INSERT INTO reports (reporter_id, target_type, target_id, reason) VALUES (?, 'STORY', ?, 'HATE')", ian, target);
        jdbc.update("INSERT INTO reports (reporter_id, target_type, target_id, reason) VALUES (?, 'STORY', ?, 'SPAM')", sam, target);

        assertThat(reports.countOpenReporters(com.caraoke.report.ReportTarget.STORY, target)).isEqualTo(2);
        assertThat(reports.existsByReporterIdAndTargetTypeAndTargetIdAndResolvedAtIsNull(
                ian, com.caraoke.report.ReportTarget.STORY, target)).isTrue();
    }

    @Test
    void momentFeedKeepsOnlyThatSlot() {
        long sam = user("sam"), song = song();
        long a = story(sam, song), b = story(sam, song), c = story(sam, song), d = story(sam, song);
        jdbc.update("UPDATE stories SET moment_sec = 130 WHERE id = ?", a);
        jdbc.update("UPDATE stories SET moment_sec = 134 WHERE id = ?", b);
        jdbc.update("UPDATE stories SET moment_sec = 135 WHERE id = ?", c);   // next dot over
        // d has no moment

        assertThat(ids(stories.findBySongIdAndStatusAndMomentSecBetweenAndIdLessThanAndUserIdNotInOrderByIdDesc(
                song, StoryStatus.VISIBLE, 130, 134, Long.MAX_VALUE, List.of(-1L), Limit.of(10))))
                .containsExactly(b, a);
    }

    // ---- moderation -------------------------------------------------------------

    @Test
    void queueGroupsOpenReportsPerTargetOldestFirstWithKeysetPaging() {
        long ian = user("ian"), sam = user("sam"), joe = user("joe"), song = song();
        long first = story(joe, song), second = story(joe, song);
        report(ian, "STORY", first, "SPAM");
        report(sam, "STORY", first, "HATE");
        report(ian, "STORY", first, "SPAM");                     // same person twice: one reporter
        report(ian, "USER", joe, "IMPERSONATION");
        long resolved = report(sam, "STORY", second, "SPAM");
        jdbc.update("UPDATE reports SET resolved_at = now() WHERE id = ?", resolved);   // closed: not in the queue

        var page = reports.findQueue(0L, 10);
        assertThat(page).extracting(r -> r.getTargetType() + ":" + r.getTargetId())
                .containsExactly("STORY:" + first, "USER:" + joe);
        assertThat(page.get(0).getReporters()).isEqualTo(2L);
        assertThat(page.get(0).getReasons().split(",")).containsExactlyInAnyOrder("HATE", "SPAM");
        assertThat(page.get(0).getLastReportedMillis()).isPositive();

        assertThat(reports.findQueue(page.get(0).getFirstReportId(), 10))
                .extracting(r -> r.getTargetType()).containsExactly("USER");
    }

    @Test
    void resolvingClosesOnlyThatTargetsOpenReports() {
        long ian = user("ian"), sam = user("sam"), joe = user("joe"), song = song();
        long target = story(joe, song), other = story(joe, song);
        report(ian, "STORY", target, "SPAM");
        report(sam, "STORY", target, "SPAM");
        report(ian, "STORY", other, "SPAM");

        int closed = reports.resolveOpen(com.caraoke.report.ReportTarget.STORY, target,
                com.caraoke.report.Resolution.ACTIONED, ian, java.time.Instant.now());

        assertThat(closed).isEqualTo(2);
        assertThat(reports.countOpenReporters(com.caraoke.report.ReportTarget.STORY, target)).isZero();
        assertThat(reports.countOpenReporters(com.caraoke.report.ReportTarget.STORY, other)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT resolution FROM reports WHERE target_id = ? LIMIT 1", String.class, target))
                .isEqualTo("ACTIONED");
    }

    @Test
    void aBanTakesDownEveryStoryButOnlyTheirs() {
        long sam = user("sam"), joe = user("joe"), song = song();
        long a = story(sam, song), b = story(sam, song), keep = story(joe, song);
        jdbc.update("UPDATE stories SET status = 'HIDDEN' WHERE id = ?", b);

        assertThat(stories.removeAllByUserId(sam)).isEqualTo(2);
        assertThat(jdbc.queryForList("SELECT status FROM stories WHERE id IN (?, ?)", String.class, a, b))
                .containsOnly("REMOVED");
        assertThat(jdbc.queryForObject("SELECT status FROM stories WHERE id = ?", String.class, keep)).isEqualTo("VISIBLE");
    }

    @Test
    void voidedStrikesDontCount() {
        long sam = user("sam"), ian = user("ian");
        jdbc.update("INSERT INTO strikes (user_id, reason, issued_by) VALUES (?, 'SPAM', ?), (?, 'HATE', ?)", sam, ian, sam, ian);
        assertThat(strikes.countByUserIdAndVoidedAtIsNull(sam)).isEqualTo(2);

        strikes.voidAll(sam, java.time.Instant.now());

        assertThat(strikes.countByUserIdAndVoidedAtIsNull(sam)).isZero();
        assertThat(strikes.findTop20ByUserIdOrderByIdDesc(sam)).hasSize(2).allMatch(s -> s.getVoidedAt() != null);
    }

    private long report(long reporter, String type, long target, String reason) {
        return jdbc.queryForObject(
                "INSERT INTO reports (reporter_id, target_type, target_id, reason) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, reporter, type, target, reason);
    }

    // ---- my page ----------------------------------------------------------------

    @Autowired com.caraoke.page.PageRaisedSongRepository raisedSongs;

    @Test
    void lifeInSongsIsOldestMemoryFirstAndSkipsStoriesWithoutAYear() {
        long sam = user("sam"), song = song();
        long y2009 = storyWith(sam, song, "prom", 2009), y1998 = storyWith(sam, song, "first cd", 1998);
        storyWith(sam, song, "no year", null);
        long hidden = storyWith(sam, song, "hidden", 2001);
        jdbc.update("UPDATE stories SET status = 'HIDDEN' WHERE id = ?", hidden);

        assertThat(ids(stories.findTop60ByUserIdAndStatusAndYearOfMemoryNotNullOrderByYearOfMemoryAscIdAsc(sam, StoryStatus.VISIBLE)))
                .containsExactly(y1998, y2009);
    }

    @Test
    void raisedSongsComeBackInOrderAndReplaceCleanly() {
        long sam = user("sam");
        long a = song(), b = song(), c = song();
        jdbc.update("INSERT INTO page_raised_songs (user_id, position, song_id) VALUES (?, 0, ?), (?, 1, ?)", sam, b, sam, a);

        assertThat(raisedSongs.findByUserIdOrderByPositionAsc(sam)).extracting(r -> r.getSongId()).containsExactly(b, a);

        raisedSongs.deleteAllForUser(sam);
        jdbc.update("INSERT INTO page_raised_songs (user_id, position, song_id) VALUES (?, 0, ?)", sam, c);
        assertThat(raisedSongs.findByUserIdOrderByPositionAsc(sam)).extracting(r -> r.getSongId()).containsExactly(c);
    }

    @Test
    void deletingAPinnedStoryUnpinsIt() {
        long sam = user("sam"), song = song();
        long pinned = story(sam, song);
        jdbc.update("INSERT INTO pages (user_id, pinned_story_id) VALUES (?, ?)", sam, pinned);

        jdbc.update("DELETE FROM stories WHERE id = ?", pinned);

        assertThat(jdbc.queryForObject("SELECT pinned_story_id FROM pages WHERE user_id = ?", Long.class, sam)).isNull();
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
