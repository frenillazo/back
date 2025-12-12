package com.acainfo.reservation.application.port.out;

import com.acainfo.reservation.application.dto.ReservationFilters;
import com.acainfo.reservation.domain.model.OnlineRequestStatus;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.acainfo.reservation.domain.model.SessionReservation;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Output port for SessionReservation persistence.
 * Defines the contract for reservation repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface ReservationRepositoryPort {

    /**
     * Save or update a reservation.
     *
     * @param reservation Domain reservation to persist
     * @return Persisted reservation with ID
     */
    SessionReservation save(SessionReservation reservation);

    /**
     * Save multiple reservations.
     *
     * @param reservations List of reservations to persist
     * @return List of persisted reservations
     */
    List<SessionReservation> saveAll(List<SessionReservation> reservations);

    /**
     * Find reservation by ID.
     *
     * @param id Reservation ID
     * @return Optional containing the reservation if found
     */
    Optional<SessionReservation> findById(Long id);

    /**
     * Find reservations with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of reservations matching filters
     */
    Page<SessionReservation> findWithFilters(ReservationFilters filters);

    /**
     * Find all reservations for a session.
     *
     * @param sessionId Session ID
     * @return List of reservations
     */
    List<SessionReservation> findBySessionId(Long sessionId);

    /**
     * Find all reservations for a student.
     *
     * @param studentId Student ID
     * @return List of reservations
     */
    List<SessionReservation> findByStudentId(Long studentId);

    /**
     * Find reservation by student and session.
     *
     * @param studentId Student ID
     * @param sessionId Session ID
     * @return Optional containing the reservation if found
     */
    Optional<SessionReservation> findByStudentIdAndSessionId(Long studentId, Long sessionId);

    /**
     * Find reservations by session and status.
     *
     * @param sessionId Session ID
     * @param status Reservation status
     * @return List of reservations
     */
    List<SessionReservation> findBySessionIdAndStatus(Long sessionId, ReservationStatus status);

    /**
     * Find reservations with pending online requests for sessions taught by a teacher.
     *
     * @param teacherId Teacher ID
     * @return List of reservations with pending requests
     */
    List<SessionReservation> findPendingOnlineRequestsByTeacherId(Long teacherId);

    /**
     * Find reservations by online request status.
     *
     * @param status Online request status
     * @return List of reservations
     */
    List<SessionReservation> findByOnlineRequestStatus(OnlineRequestStatus status);

    /**
     * Check if a reservation exists for student and session.
     *
     * @param studentId Student ID
     * @param sessionId Session ID
     * @return true if reservation exists
     */
    boolean existsByStudentIdAndSessionId(Long studentId, Long sessionId);

    /**
     * Count confirmed reservations for a session by mode.
     *
     * @param sessionId Session ID
     * @param mode Reservation mode (IN_PERSON or ONLINE)
     * @return Number of confirmed reservations with that mode
     */
    long countBySessionIdAndStatusAndMode(Long sessionId, ReservationStatus status, ReservationMode mode);

    /**
     * Count confirmed in-person reservations for a session.
     * Convenience method for checking seat availability.
     *
     * @param sessionId Session ID
     * @return Number of confirmed in-person reservations
     */
    default long countInPersonReservations(Long sessionId) {
        return countBySessionIdAndStatusAndMode(sessionId, ReservationStatus.CONFIRMED, ReservationMode.IN_PERSON);
    }

    /**
     * Find reservations without attendance recorded for a session.
     *
     * @param sessionId Session ID
     * @return List of reservations without attendance
     */
    List<SessionReservation> findBySessionIdAndAttendanceStatusIsNull(Long sessionId);

    /**
     * Delete a reservation.
     *
     * @param id Reservation ID
     */
    void delete(Long id);
}
