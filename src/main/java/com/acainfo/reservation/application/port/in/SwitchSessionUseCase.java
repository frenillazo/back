package com.acainfo.reservation.application.port.in;

import com.acainfo.reservation.application.dto.SwitchSessionCommand;
import com.acainfo.reservation.domain.model.SessionReservation;

/**
 * Use case for switching a reservation to a different session.
 * Input port defining the contract for session switching.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>New session must be for the same subject as the enrollment</li>
 *   <li>New session must have available seats (for IN_PERSON)</li>
 *   <li>Current reservation is cancelled, new one is created</li>
 * </ul>
 */
public interface SwitchSessionUseCase {

    /**
     * Switch a reservation to a different session.
     *
     * @param command Switch data (studentId, currentReservationId, newSessionId)
     * @return The new reservation
     * @throws com.acainfo.reservation.domain.exception.ReservationNotFoundException if current not found
     * @throws com.acainfo.reservation.domain.exception.SessionFullException if no seats in new session
     * @throws com.acainfo.reservation.domain.exception.CrossGroupReservationNotAllowedException if different subject
     */
    SessionReservation switchSession(SwitchSessionCommand command);
}
