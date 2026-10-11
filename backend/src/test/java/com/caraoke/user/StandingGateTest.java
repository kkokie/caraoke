package com.caraoke.user;

import com.caraoke.user.UserDtos.AccountStanding;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class StandingGateTest {

    private static final Instant NEXT_WEEK = Instant.parse("2026-10-18T19:00:00Z");

    @Test
    void goodAndWarnedPassEverything() {
        assertThat(StandingGate.refusal(new AccountStanding(Standing.GOOD, null, false), true)).isEmpty();
        assertThat(StandingGate.refusal(new AccountStanding(Standing.WARNED, null, false), true)).isEmpty();
    }

    @Test
    void suspendedCanReadButNotWrite() {
        AccountStanding s = new AccountStanding(Standing.SUSPENDED, NEXT_WEEK, false);
        assertThat(StandingGate.refusal(s, false)).isEmpty();
        assertThat(StandingGate.refusal(s, true)).hasValueSatisfying(m -> assertThat(m).contains("Oct 18"));
    }

    @Test
    void bannedCantDoAnything() {
        AccountStanding s = new AccountStanding(Standing.BANNED, null, false);
        assertThat(StandingGate.refusal(s, false)).isPresent();
        assertThat(StandingGate.refusal(s, true)).isPresent();
    }

    @Test
    void anEndedSuspensionReadsAsAWarning() {
        Instant later = NEXT_WEEK.plusSeconds(1);
        assertThat(Standing.SUSPENDED.effective(NEXT_WEEK, later)).isEqualTo(Standing.WARNED);
        assertThat(Standing.SUSPENDED.effective(NEXT_WEEK, NEXT_WEEK.minusSeconds(1))).isEqualTo(Standing.SUSPENDED);
        assertThat(Standing.BANNED.effective(null, later)).isEqualTo(Standing.BANNED);
    }
}
