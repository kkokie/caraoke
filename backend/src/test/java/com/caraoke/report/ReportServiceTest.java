package com.caraoke.report;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    private final ReportRepository reports = mock(ReportRepository.class);
    private final ReportService service = new ReportService(reports, java.time.Clock.systemUTC());

    @Test
    void reportingTwiceIsCountedOnce() {
        when(reports.existsByReporterIdAndTargetTypeAndTargetIdAndResolvedAtIsNull(5L, ReportTarget.STORY, 20L))
                .thenReturn(false, true);
        when(reports.countOpenReporters(ReportTarget.STORY, 20L)).thenReturn(1L);

        service.file(5L, ReportTarget.STORY, 20L, ReportReason.SPAM, null);
        service.file(5L, ReportTarget.STORY, 20L, ReportReason.SPAM, null);

        verify(reports, times(1)).save(any(Report.class));
    }

    @Test
    void detailsAreTrimmedAndCapped() {
        assertThat(ReportService.clean("  ")).isNull();
        assertThat(ReportService.clean(" spam ")).isEqualTo("spam");
        assertThat(ReportService.clean("x".repeat(600))).hasSize(ReportService.MAX_DETAILS);
    }
}
