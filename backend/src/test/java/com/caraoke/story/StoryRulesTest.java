package com.caraoke.story;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoryRulesTest {

    private static final Year THIS_YEAR = Year.of(2026);

    @Test
    void bodyIsTrimmed() {
        assertThat(StoryRules.body("  first dance at prom \n")).isEqualTo("first dance at prom");
    }

    @Test
    void bodyMustHaveTextAndFitTheLimit() {
        assertBadRequest(() -> StoryRules.body("   "));
        assertBadRequest(() -> StoryRules.body(null));
        assertBadRequest(() -> StoryRules.body("x".repeat(StoryRules.MAX_BODY + 1)));
        assertThat(StoryRules.body("x".repeat(StoryRules.MAX_BODY))).hasSize(StoryRules.MAX_BODY);
    }

    @Test
    void momentIsOptionalAndMustBeInsideTheSong() {
        assertThat(StoryRules.moment(null, 223)).isNull();
        assertThat(StoryRules.moment(134, 223)).isEqualTo(134);
        assertThat(StoryRules.moment(223, 223)).isEqualTo(223);          // the very last second is fine
        assertBadRequest(() -> StoryRules.moment(224, 223));
        assertBadRequest(() -> StoryRules.moment(-1, 223));
    }

    @Test
    void momentIsAcceptedWhenSongLengthIsUnknown() {
        assertThat(StoryRules.moment(9999, null)).isEqualTo(9999);
    }

    @Test
    void yearIsOptionalAndCantBeInTheFuture() {
        assertThat(StoryRules.year(null, THIS_YEAR)).isNull();
        assertThat(StoryRules.year(2009, THIS_YEAR)).isEqualTo((short) 2009);
        assertThat(StoryRules.year(2026, THIS_YEAR)).isEqualTo((short) 2026);
        assertBadRequest(() -> StoryRules.year(2027, THIS_YEAR));
        assertBadRequest(() -> StoryRules.year(1899, THIS_YEAR));
    }

    private static void assertBadRequest(Runnable call) {
        assertThatThrownBy(call::run)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(400));
    }
}
