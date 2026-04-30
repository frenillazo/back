package com.acainfo.intensive.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Intensive domain entity (anemic model with Lombok).
 *
 * <p>Represents an intensive course: short-format teaching with non-recurring sessions
 * (specific dates &amp; times rather than weekly schedules). Intensives manage their
 * sessions directly via the {@code session} module without using {@code Schedule}.</p>
 *
 * <p>Differences with {@code SubjectGroup} (regular course):</p>
 * <ul>
 *   <li>No {@code Schedule}: sessions are created ad-hoc by the admin (calendar / bulk).</li>
 *   <li>Default capacity: 50 (vs 24 for regular).</li>
 *   <li>Reservations: students always assigned ONLINE (no in-person quota).</li>
 *   <li>Payment type: always {@code INTENSIVE_FULL} (single payment for the whole course).</li>
 *   <li>{@code startDate} / {@code endDate} delimit the date range in which sessions can exist.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Intensive {

    /** Default maximum capacity for intensive courses. */
    public static final int DEFAULT_MAX_CAPACITY = 50;

    /** Default price per hour (€/hour) when not customised. */
    public static final BigDecimal DEFAULT_PRICE_PER_HOUR = new BigDecimal("15.00");

    private Long id;
    private String name;                        // Auto-generated: "[subjectName] intensivo N YY-YY"
    private Long subjectId;
    private Long teacherId;
    private IntensiveStatus status;
    private BigDecimal pricePerHour;            // Optional: null = use default

    @Builder.Default
    private Integer currentEnrollmentCount = 0;

    private Integer capacity;                   // Optional: null = use default (50)

    private LocalDate startDate;                // Inclusive
    private LocalDate endDate;                  // Inclusive

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Query Methods ====================

    public boolean isOpen() {
        return status == IntensiveStatus.OPEN;
    }

    public boolean isClosed() {
        return status == IntensiveStatus.CLOSED;
    }

    public boolean isCancelled() {
        return status == IntensiveStatus.CANCELLED;
    }

    /**
     * Maximum number of students this intensive can host.
     * Returns the custom capacity if configured, otherwise {@link #DEFAULT_MAX_CAPACITY}.
     */
    public int getMaxCapacity() {
        return capacity != null ? capacity : DEFAULT_MAX_CAPACITY;
    }

    public int getAvailableSeats() {
        return Math.max(0, getMaxCapacity() - currentEnrollmentCount);
    }

    public boolean hasAvailableSeats() {
        return getAvailableSeats() > 0;
    }

    public boolean isFull() {
        return currentEnrollmentCount >= getMaxCapacity();
    }

    /**
     * An intensive can accept enrollments only when OPEN and with seats available.
     */
    public boolean canEnroll() {
        return isOpen() && hasAvailableSeats();
    }

    public BigDecimal getEffectivePricePerHour() {
        return pricePerHour != null ? pricePerHour : DEFAULT_PRICE_PER_HOUR;
    }

    /**
     * Whether {@code date} is contained in the [startDate, endDate] range (both inclusive).
     */
    public boolean containsDate(LocalDate date) {
        if (date == null || startDate == null || endDate == null) return false;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
