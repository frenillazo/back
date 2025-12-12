package com.acainfo.reservation.infrastructure.adapter.out.persistence.repository;

import com.acainfo.reservation.domain.model.OnlineRequestStatus;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.acainfo.reservation.infrastructure.adapter.out.persistence.entity.SessionReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for SessionReservationJpaEntity.
 * Extends JpaSpecificationExecutor for Criteria Builder support.
 */
@Repository
public interface JpaReservationRepository extends
        JpaRepository<SessionReservationJpaEntity, Long>,
        JpaSpecificationExecutor<SessionReservationJpaEntity> {

    /**
     * Find all reservations for a session.
     */
    List<SessionReservationJpaEntity> findBySessionId(Long sessionId);

    /**
     * Find all reservations for a student.
     */
    List<SessionReservationJpaEntity> findByStudentId(Long studentId);

    /**
     * Find reservation by student and session.
     */
    Optional<SessionReservationJpaEntity> findByStudentIdAndSessionId(Long studentId, Long sessionId);

    /**
     * Find reservations by session and status.
     */
    List<SessionReservationJpaEntity> findBySessionIdAndStatus(Long sessionId, ReservationStatus status);

    /**
     * Find reservations by online request status.
     */
    List<SessionReservationJpaEntity> findByOnlineRequestStatus(OnlineRequestStatus status);

    /**
     * Check if a reservation exists for student and session.
     */
    boolean existsByStudentIdAndSessionId(Long studentId, Long sessionId);

    /**
     * Count reservations for a session by status and mode.
     */
    long countBySessionIdAndStatusAndMode(Long sessionId, ReservationStatus status, ReservationMode mode);

    /**
     * Find reservations without attendance recorded for a session.
     */
    List<SessionReservationJpaEntity> findBySessionIdAndAttendanceStatusIsNull(Long sessionId);

    /**
     * Find pending online requests for sessions taught by a specific teacher.
     * Joins with session table to find sessions where the group's teacher matches.
     */
    @Query("""
        SELECT r FROM SessionReservationJpaEntity r
        JOIN SessionJpaEntity s ON r.sessionId = s.id
        JOIN SubjectGroupJpaEntity g ON s.groupId = g.id
        WHERE r.onlineRequestStatus = 'PENDING'
        AND g.teacherId = :teacherId
        AND r.status = 'CONFIRMED'
        ORDER BY r.onlineRequestedAt ASC
        """)
    List<SessionReservationJpaEntity> findPendingOnlineRequestsByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * Find reservations by enrollment.
     */
    List<SessionReservationJpaEntity> findByEnrollmentId(Long enrollmentId);
}
