package com.songstories.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HandleRulesTest {

    @Test
    void normalizesCaseWhitespaceAndLeadingAt() {
        assertThat(HandleRules.normalize("  @Ian_K ")).isEqualTo("ian_k");
    }

    @Test
    void acceptsValidHandles() {
        assertThat(HandleRules.validate("ian_k")).isNull();
        assertThat(HandleRules.validate("abc")).isNull();
        assertThat(HandleRules.validate("a".repeat(20))).isNull();
    }

    @Test
    void rejectsBadFormat() {
        assertThat(HandleRules.validate("ab")).isNotNull();               // too short
        assertThat(HandleRules.validate("a".repeat(21))).isNotNull();     // too long
        assertThat(HandleRules.validate("ian.k")).isNotNull();            // bad char
        assertThat(HandleRules.validate("ian k")).isNotNull();
    }

    @Test
    void rejectsReserved() {
        assertThat(HandleRules.validate("admin")).isEqualTo("That handle is reserved.");
        assertThat(HandleRules.validate("songstories")).isNotNull();
    }
}
