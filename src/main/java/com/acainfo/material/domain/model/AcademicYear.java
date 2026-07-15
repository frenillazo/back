package com.acainfo.material.domain.model;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;

/**
 * Academic year helper. An academic year is identified by its start year
 * (2025 = "2025-26" course) with a September→August cutoff: months >= September
 * belong to the natural year, months <= August to the previous one.
 */
public final class AcademicYear {

    private AcademicYear() {
        // Utility class
    }

    /**
     * Current academic year according to the given clock.
     * The clock is injected (never {@code Clock.systemDefaultZone()} inline) so tests can fix it.
     */
    public static int current(Clock clock) {
        LocalDate today = LocalDate.now(clock);
        return today.getMonth().compareTo(Month.SEPTEMBER) >= 0
                ? today.getYear()
                : today.getYear() - 1;
    }
}
