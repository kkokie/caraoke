package com.caraoke.resonance;

/** What a viewer sees under a story: how many felt it, and whether they did. */
public record ResonanceSummary(long count, boolean mine) {

    public static final ResonanceSummary NONE = new ResonanceSummary(0, false);
}
