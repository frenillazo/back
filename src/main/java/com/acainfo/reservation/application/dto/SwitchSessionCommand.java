package com.acainfo.reservation.application.dto;

/**
 * Command to switch a reservation to a different session.
 * Used when a student wants to attend a different group's session (same subject).
 *
 * @param studentId ID of the student
 * @param currentReservationId ID of the current reservation to cancel
 * @param newSessionId ID of the new session to reserve
 */
public record SwitchSessionCommand(
        Long studentId,
        Long currentReservationId,
        Long newSessionId
) {
}
