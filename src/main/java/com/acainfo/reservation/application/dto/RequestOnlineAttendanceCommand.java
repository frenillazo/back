package com.acainfo.reservation.application.dto;

/**
 * Command to request online attendance for a session.
 * Used when a student with an in-person reservation wants to attend online instead.
 * Must be submitted at least 6 hours before the session.
 *
 * @param reservationId ID of the reservation
 * @param studentId ID of the student making the request
 */
public record RequestOnlineAttendanceCommand(
        Long reservationId,
        Long studentId
) {
}
