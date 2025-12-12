package com.acainfo.reservation.application.port.in;

import com.acainfo.reservation.application.dto.RequestOnlineAttendanceCommand;
import com.acainfo.reservation.domain.model.SessionReservation;

/**
 * Use case for requesting online attendance for a session.
 * Input port defining the contract for online attendance requests.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Only for regular group sessions (not intensive)</li>
 *   <li>Must be submitted at least 6 hours before the session</li>
 *   <li>Reservation must be IN_PERSON mode</li>
 *   <li>No existing pending request</li>
 * </ul>
 */
public interface RequestOnlineAttendanceUseCase {

    /**
     * Request to change from in-person to online attendance.
     *
     * @param command Request data (reservationId, studentId)
     * @return The updated reservation with pending online request
     * @throws com.acainfo.reservation.domain.exception.ReservationNotFoundException if not found
     * @throws com.acainfo.reservation.domain.exception.OnlineRequestTooLateException if less than 6 hours
     * @throws com.acainfo.reservation.domain.exception.OnlineRequestAlreadyExistsException if already requested
     * @throws com.acainfo.reservation.domain.exception.InvalidReservationStateException if not IN_PERSON
     */
    SessionReservation requestOnline(RequestOnlineAttendanceCommand command);
}
