package com.acainfo.material.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for {@link AcademicYear}.
 * The academic year cutoff is September→August: August 31st still belongs to the
 * previous academic year; September 1st starts the new one.
 */
class AcademicYearTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    private static Clock fixedAt(String isoInstant) {
        return Clock.fixed(Instant.parse(isoInstant), UTC);
    }

    @Test
    void august31BelongsToPreviousAcademicYear() {
        Clock clock = fixedAt("2026-08-31T12:00:00Z");

        assertThat(AcademicYear.current(clock)).isEqualTo(2025);
    }

    @Test
    void september1StartsNewAcademicYear() {
        Clock clock = fixedAt("2026-09-01T00:00:00Z");

        assertThat(AcademicYear.current(clock)).isEqualTo(2026);
    }

    @Test
    void springMonthsBelongToPreviousAcademicYear() {
        Clock clock = fixedAt("2026-02-15T12:00:00Z");

        assertThat(AcademicYear.current(clock)).isEqualTo(2025);
    }

    @Test
    void decemberBelongsToCurrentAcademicYear() {
        Clock clock = fixedAt("2025-12-20T12:00:00Z");

        assertThat(AcademicYear.current(clock)).isEqualTo(2025);
    }
}
