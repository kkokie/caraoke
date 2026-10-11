package com.caraoke.safety;

import com.caraoke.report.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request shapes for reporting. */
public final class SafetyDtos {

    private SafetyDtos() { }

    public enum Kind { STORY, USER }

    /** Report a story (`storyId`) or a person (`handle`). */
    public record ReportRequest(
            @NotNull Kind kind,
            Long storyId,
            String handle,
            @NotNull ReportReason reason,
            @Size(max = 500) String details) { }
}
