package com.acainfo.reservation.application.dto;

/**
 * Command to generate reservations for all enrolled students when sessions are created.
 * Called automatically when sessions are generated from schedules.
 *
 * @param sessionId ID of the newly created session
 * @param groupId ID of the group (to find enrolled students)
 */
public record GenerateReservationsCommand(
        Long sessionId,
        Long groupId
) {
}
