package com.caraoke.story;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContentRulesTest {

    @Test
    void linksAreRefused() {
        bad("listen at https://spam.example");
        bad("go to www.cheap-pills.biz now");
        bad("follow me on mysite.com");
        bad("bit.ly/abc");
    }

    @Test
    void ordinaryStoriesPass() {
        ok("Mr. Brightside came on at 2 a.m. and we screamed every word.");
        ok("St. Louis, 2009. Dad’s truck.");
        ok("The song was 3:43 long and I played it 4.5 times a day.");
        ok("aaaah I loved this so much!!!");
    }

    @Test
    void keyboardMashIsRefused() {
        bad("a".repeat(15));
        bad("great song" + "!".repeat(20));
    }

    private static void bad(String text) {
        assertThatThrownBy(() -> ContentRules.check(text)).isInstanceOf(ResponseStatusException.class);
    }

    private static void ok(String text) {
        assertThatCode(() -> ContentRules.check(text)).doesNotThrowAnyException();
    }
}
