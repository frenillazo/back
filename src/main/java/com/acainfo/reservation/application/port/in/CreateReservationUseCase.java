package com.acainfo.reservation.application.port.in;

import com.acainfo.reservation.application.dto.CreateReservationCommand;
import com.acainfo.reservation.domain.model.SessionReservation;

/**
 * Use case for creating a new session reservation.
 * Input port defining the contract for reservation creation.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Student must be enrolled in a group of the same subject</li>
 *   <li>For IN_PERSON mode, session must have available seats (max 24)</li>
 *   <li>One reservation per student per session</li>
 * </ul>
 */
public interface CreateReservationUseCase {

    /**
     * Create a reservation for a student in a session.
     *
     * @param command Reservation data (studentId, sessionId, enrollmentId, mode)
     * @return The created reservation
     * @throws com.acainfo.reservation.domain.exception.ReservationAlreadyExistsException if already reserved
     * @throws com.acainfo.reservation.domain.exception.SessionFullException if no in-person seats available
     * @throws com.acainfo.reservation.domain.exception.CrossGroupReservationNotAllowedException if different subject
     */
    SessionReservation create(CreateReservationCommand command);
}
