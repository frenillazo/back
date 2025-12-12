package com.acainfo.reservation.application.port.in;

import com.acainfo.reservation.application.dto.ProcessOnlineRequestCommand;
import com.acainfo.reservation.domain.model.SessionReservation;

/**
 * Use case for processing (approving/rejecting) online attendance requests.
 * Input port defining the contract for teachers to handle requests.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Only pending requests can be processed</li>
 *   <li>If approved, reservation mode changes to ONLINE</li>
 *   <li>If rejected, reservation stays IN_PERSON</li>
 * </ul>
 */
public interface ProcessOnlineRequestUseCase {

    /**
     * Process an online attendance request.
     *
     * @param command Process data (reservationId, teacherId, approved)
     * @return The updated reservation
     * @throws com.acainfo.reservation.domain.exception.ReservationNotFoundException if not found
     * @throws com.acainfo.reservation.domain.exception.InvalidReservationStateException if not pending
     */
    SessionReservation process(ProcessOnlineRequestCommand command);
}
