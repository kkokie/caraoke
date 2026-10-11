package com.caraoke.moderation;

import com.caraoke.user.Standing;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StrikeLadderTest {

    @Test
    void warnThenSuspendThenBan() {
        assertThat(StrikeLadder.standingFor(0)).isEqualTo(Standing.GOOD);
        assertThat(StrikeLadder.standingFor(1)).isEqualTo(Standing.WARNED);
        assertThat(StrikeLadder.standingFor(2)).isEqualTo(Standing.SUSPENDED);
        assertThat(StrikeLadder.standingFor(3)).isEqualTo(Standing.BANNED);
        assertThat(StrikeLadder.standingFor(9)).isEqualTo(Standing.BANNED);
    }
}
