package com.acainfo.reservation.application.port.in;

import com.acainfo.reservation.domain.model.SessionReservation;

/**
 * Use case for cancelling a session reservation.
 * Input port defining the contract for reservation cancellation.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Only confirmed reservations can be cancelled</li>
 *   <li>Cannot cancel if attendance is already recorded</li>
 * </ul>
 */
public interface CancelReservationUseCase {

    /**
     * Cancel a reservation.
     *
     * @param reservationId ID of the reservation to cancel
     * @param studentId ID of the student (for authorization)
     * @return The cancelled reservation
     * @throws com.acainfo.reservation.domain.exception.ReservationNotFoundException if not found
     * @throws com.acainfo.reservation.domain.exception.InvalidReservationStateException if cannot cancel
     */
    SessionReservation cancel(Long reservationId, Long studentId);
}
