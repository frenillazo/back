package com.acainfo.reservation.application.port.in;

import com.acainfo.reservation.application.dto.ReservationFilters;
import com.acainfo.reservation.domain.model.SessionReservation;
import com.acainfo.shared.application.dto.PageResponse;

import java.util.List;

/**
 * Use case for querying reservations.
 * Input port defining the contract for reservation queries.
 */
public interface GetReservationUseCase {

    /**
     * Get a reservation by ID.
     *
     * @param id Reservation ID
     * @return The reservation
     * @throws com.acainfo.reservation.domain.exception.ReservationNotFoundException if not found
     */
    SessionReservation getById(Long id);

    /**
     * Find reservations with dynamic filters.
     *
     * @param filters Filter criteria
     * @return Page of reservations matching filters
     */
    PageResponse<SessionReservation> findWithFilters(ReservationFilters filters);

    /**
     * Get all reservations for a session.
     *
     * @param sessionId Session ID
     * @return List of reservations for the session
     */
    List<SessionReservation> getBySessionId(Long sessionId);

    /**
     * Get all reservations for a student.
     *
     * @param studentId Student ID
     * @return List of reservations for the student
     */
    List<SessionReservation> getByStudentId(Long studentId);

    /**
     * Get pending online requests for a teacher's sessions.
     *
     * @param teacherId Teacher ID
     * @return List of reservations with pending online requests
     */
    List<SessionReservation> getPendingOnlineRequestsForTeacher(Long teacherId);
}
