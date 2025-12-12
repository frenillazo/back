package com.acainfo.reservation.application.dto;

/**
 * Command to process (approve/reject) an online attendance request.
 * Used by teachers to handle student requests.
 *
 * @param reservationId ID of the reservation with the pending request
 * @param teacherId ID of the teacher processing the request
 * @param approved Whether to approve (true) or reject (false)
 */
public record ProcessOnlineRequestCommand(
        Long reservationId,
        Long teacherId,
        boolean approved
) {
}
