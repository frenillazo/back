package com.acainfo.group.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SubjectGroup domain entity (anemic model with Lombok).
 *
 * <p>Represents a regular group of students taking a subject with a specific teacher.
 * Sessions for regular groups are derived from {@code Schedule}s (weekly recurrence)
 * and bound by {@code [startDate, endDate]}.</p>
 *
 * <p>Intensive courses live in a separate entity ({@code com.acainfo.intensive.Intensive}).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class SubjectGroup {

    /** Default maximum capacity for regular groups. */
    public static final int DEFAULT_MAX_CAPACITY = 24;

    /** Default price per hour (€/hour) when not customised. */
    public static final BigDecimal DEFAULT_PRICE_PER_HOUR = new BigDecimal("15.00");

    private Long id;
    private String name;                         // Auto-generated: "[subjectName] grupo N YY-YY"
    private Long subjectId;
    private Long teacherId;
    private GroupStatus status;                  // OPEN, CLOSED, CANCELLED
    private BigDecimal pricePerHour;             // Optional: null = use default

    @Builder.Default
    private Integer currentEnrollmentCount = 0;

    private Integer capacity;                    // Optional: null = use default (24)

    private LocalDate startDate;                 // Inclusive — first day sessions can be generated
    private LocalDate endDate;                   // Inclusive — last day sessions can be generated

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Query Methods ====================

    public boolean isOpen() {
        return status == GroupStatus.OPEN;
    }

    public boolean isClosed() {
        return status == GroupStatus.CLOSED;
    }

    public boolean isCancelled() {
        return status == GroupStatus.CANCELLED;
    }

    /**
     * Maximum students this group can host. Returns the custom capacity if configured,
     * otherwise {@link #DEFAULT_MAX_CAPACITY}.
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
