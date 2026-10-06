package com.caraoke.resonance;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeltCursorTest {

    @Test
    void roundTripsWithMicrosecondPrecision() {
        FeltCursor c = new FeltCursor(Instant.parse("2026-10-05T20:31:07.123456Z"), 42L);

        assertThat(c.encode()).isEqualTo("2026-10-05T20:31:07.123456Z_42");
        assertThat(FeltCursor.decode(c.encode())).isEqualTo(c);
    }

    @Test
    void emptyMeansFirstPage() {
        assertThat(FeltCursor.decode(null)).isNull();
        assertThat(FeltCursor.decode("  ")).isNull();
    }

    @Test
    void garbageIs400() {
        for (String bad : new String[] {"nope", "_42", "2026-10-05_x", "not-a-time_42"}) {
            assertThatThrownBy(() -> FeltCursor.decode(bad))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(400));
        }
    }
}
