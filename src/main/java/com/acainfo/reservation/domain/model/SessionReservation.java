package com.acainfo.reservation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * SessionReservation domain entity - Anemic model with Lombok.
 * Represents a student's reservation for a specific session.
 *
 * <p>Business rules (enforced in application services):</p>
 * <ul>
 *   <li>Auto-generated for enrolled students when sessions are created from schedules</li>
 *   <li>Students can switch to another session of the same subject (different course) if seats available</li>
 *   <li>In-person attendance limited to classroom capacity (typically 24)</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class SessionReservation {

    private Long id;

    /**
     * Reference to the student (User with STUDENT role).
     */
    private Long studentId;

    /**
     * Reference to the session this reservation is for.
     */
    private Long sessionId;

    /**
     * Reference to the student's original enrollment.
     * Kept for traceability even when attending a different course's session.
     */
    private Long enrollmentId;

    /**
     * Mode of attendance: IN_PERSON or ONLINE.
     */
    private ReservationMode mode;

    /**
     * Status of the reservation.
     */
    private ReservationStatus status;

    /**
     * When the reservation was created.
     */
    private LocalDateTime reservedAt;

    /**
     * When the reservation was cancelled (if applicable).
     */
    private LocalDateTime cancelledAt;

    // ==================== Audit Fields ====================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Query Methods ====================

    /**
     * Check if reservation is confirmed.
     */
    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }

    /**
     * Check if reservation is cancelled.
     */
    public boolean isCancelled() {
        return status == ReservationStatus.CANCELLED;
    }

    /**
     * Check if attending in person.
     */
    public boolean isInPerson() {
        return mode == ReservationMode.IN_PERSON;
    }

    /**
     * Check if attending online.
     */
    public boolean isOnline() {
        return mode == ReservationMode.ONLINE;
    }

    /**
     * Check if reservation can be cancelled.
     */
    public boolean canBeCancelled() {
        return isConfirmed();
    }
}
