package com.acainfo.reservation.application.dto;

import com.acainfo.reservation.domain.model.ReservationMode;

/**
 * Command to create a new session reservation.
 * Used when a student wants to reserve a seat in a session.
 *
 * @param studentId ID of the student making the reservation
 * @param sessionId ID of the session to reserve
 * @param enrollmentId ID of the student's enrollment (for traceability)
 * @param mode Attendance mode: IN_PERSON or ONLINE
 */
public record CreateReservationCommand(
        Long studentId,
        Long sessionId,
        Long enrollmentId,
        ReservationMode mode
) {
}
