package acainfo.back.attendance.infrastructure.adapters.out;

import acainfo.back.attendance.domain.model.Attendance;
import acainfo.back.attendance.domain.model.AttendanceStatus;
import acainfo.back.session.domain.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Attendance entity.
 * Provides comprehensive query methods for attendance management and statistics.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // ==================== BASIC QUERIES ====================

    /**
     * Find all attendance records for a specific session
     */
    List<Attendance> findBySession(Session session);

    /**
     * Find all attendance records for a session by ID
     */
    @Query("SELECT a FROM Attendance a WHERE a.session.id = :sessionId ORDER BY a.studentId ASC")
    List<Attendance> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Find all attendance records for a specific student
     */
    List<Attendance> findByStudentId(Long studentId);

    /**
     * Find all attendance records for a student ordered by date
     */
    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId " +
           "ORDER BY a.session.scheduledStart DESC")
    List<Attendance> findByStudentIdOrderByDate(@Param("studentId") Long studentId);

    /**
     * Find attendance for a specific student in a specific session
     */
    @Query("SELECT a FROM Attendance a WHERE a.session.id = :sessionId AND a.studentId = :studentId")
    Optional<Attendance> findBySessionIdAndStudentId(
        @Param("sessionId") Long sessionId,
        @Param("studentId") Long studentId
    );

    /**
     * Find all attendance records for a subject group
     */
    @Query("SELECT a FROM Attendance a WHERE a.session.subjectGroup.id = :groupId " +
           "ORDER BY a.session.scheduledStart DESC, a.studentId ASC")
    List<Attendance> findByGroupId(@Param("groupId") Long groupId);

    // ==================== STATUS-BASED QUERIES ====================

    /**
     * Find all attendance records with a specific status
     */
    List<Attendance> findByStatus(AttendanceStatus status);

    /**
     * Find attendance records for a student with specific status
     */
    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId AND a.status = :status " +
           "ORDER BY a.session.scheduledStart DESC")
    List<Attendance> findByStudentIdAndStatus(
        @Param("studentId") Long studentId,
        @Param("status") AttendanceStatus status
    );

    /**
     * Find attendance records for a session with specific status
     */
    @Query("SELECT a FROM Attendance a WHERE a.session.id = :sessionId AND a.status = :status")
    List<Attendance> findBySessionIdAndStatus(
        @Param("sessionId") Long sessionId,
        @Param("status") AttendanceStatus status
    );

    /**
     * Find all absences (AUSENTE or JUSTIFICADO) for a student
     */
    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.status IN ('AUSENTE', 'JUSTIFICADO') " +
           "ORDER BY a.session.scheduledStart DESC")
    List<Attendance> findAbsencesByStudentId(@Param("studentId") Long studentId);

    /**
     * Find all effective attendance (PRESENTE or TARDANZA) for a student
     */
    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.status IN ('PRESENTE', 'TARDANZA') " +
           "ORDER BY a.session.scheduledStart DESC")
    List<Attendance> findEffectiveAttendanceByStudentId(@Param("studentId") Long studentId);

    // ==================== DATE RANGE QUERIES ====================

    /**
     * Find attendance records for a student within a date range
     */
    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.session.scheduledStart >= :startDate " +
           "AND a.session.scheduledStart <= :endDate " +
           "ORDER BY a.session.scheduledStart ASC")
    List<Attendance> findByStudentIdAndDateRange(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find attendance records for a group within a date range
     */
    @Query("SELECT a FROM Attendance a WHERE a.session.subjectGroup.id = :groupId " +
           "AND a.session.scheduledStart >= :startDate " +
           "AND a.session.scheduledStart <= :endDate " +
           "ORDER BY a.session.scheduledStart ASC")
    List<Attendance> findByGroupIdAndDateRange(
        @Param("groupId") Long groupId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find attendance records recorded within a date range
     */
    @Query("SELECT a FROM Attendance a WHERE a.recordedAt >= :startDate AND a.recordedAt <= :endDate " +
           "ORDER BY a.recordedAt DESC")
    List<Attendance> findByRecordedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // ==================== EXISTENCE CHECKS ====================

    /**
     * Check if attendance exists for a student in a session
     */
    boolean existsBySessionIdAndStudentId(Long sessionId, Long studentId);

    /**
     * Check if any attendance has been recorded for a session
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendance a " +
           "WHERE a.session.id = :sessionId")
    boolean existsBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Check if a student has any attendance records
     */
    boolean existsByStudentId(Long studentId);

    // ==================== COUNT QUERIES ====================

    /**
     * Count attendance records for a session
     */
    long countBySessionId(Long sessionId);

    /**
     * Count attendance records for a student
     */
    long countByStudentId(Long studentId);

    /**
     * Count attendance records by status for a student
     */
    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    /**
     * Count attendance records by status for a session
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.session.id = :sessionId AND a.status = :status")
    long countBySessionIdAndStatus(
        @Param("sessionId") Long sessionId,
        @Param("status") AttendanceStatus status
    );

    /**
     * Count effective attendance (PRESENTE + TARDANZA) for a student
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.status IN ('PRESENTE', 'TARDANZA')")
    long countEffectiveAttendanceByStudentId(@Param("studentId") Long studentId);

    /**
     * Count absences (AUSENTE + JUSTIFICADO) for a student
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.status IN ('AUSENTE', 'JUSTIFICADO')")
    long countAbsencesByStudentId(@Param("studentId") Long studentId);

    // ==================== STATISTICS QUERIES ====================

    /**
     * Count attendance by status for a student
     * Returns pairs of [status, count]
     */
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.studentId = :studentId " +
           "GROUP BY a.status")
    List<Object[]> countByStatusForStudent(@Param("studentId") Long studentId);

    /**
     * Count attendance by status for a session
     * Returns pairs of [status, count]
     */
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.session.id = :sessionId " +
           "GROUP BY a.status")
    List<Object[]> countByStatusForSession(@Param("sessionId") Long sessionId);

    /**
     * Count attendance by status for a subject group
     * Returns pairs of [status, count]
     */
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.session.subjectGroup.id = :groupId " +
           "GROUP BY a.status")
    List<Object[]> countByStatusForGroup(@Param("groupId") Long groupId);

    /**
     * Calculate attendance rate for a student
     * Returns percentage of effective attendance (PRESENTE + TARDANZA)
     */
    @Query("SELECT " +
           "CAST(COUNT(CASE WHEN a.status IN ('PRESENTE', 'TARDANZA') THEN 1 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double) " +
           "FROM Attendance a WHERE a.studentId = :studentId")
    Double calculateAttendanceRateForStudent(@Param("studentId") Long studentId);

    /**
     * Calculate attendance rate for a student in a specific group
     */
    @Query("SELECT " +
           "CAST(COUNT(CASE WHEN a.status IN ('PRESENTE', 'TARDANZA') THEN 1 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double) " +
           "FROM Attendance a WHERE a.studentId = :studentId AND a.session.subjectGroup.id = :groupId")
    Double calculateAttendanceRateForStudentInGroup(
        @Param("studentId") Long studentId,
        @Param("groupId") Long groupId
    );

    /**
     * Calculate average attendance rate for a session
     */
    @Query("SELECT " +
           "CAST(COUNT(CASE WHEN a.status IN ('PRESENTE', 'TARDANZA') THEN 1 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double) " +
           "FROM Attendance a WHERE a.session.id = :sessionId")
    Double calculateAttendanceRateForSession(@Param("sessionId") Long sessionId);

    /**
     * Calculate average attendance rate for a group
     */
    @Query("SELECT " +
           "CAST(COUNT(CASE WHEN a.status IN ('PRESENTE', 'TARDANZA') THEN 1 END) AS double) * 100.0 / " +
           "CAST(COUNT(a) AS double) " +
           "FROM Attendance a WHERE a.session.subjectGroup.id = :groupId")
    Double calculateAttendanceRateForGroup(@Param("groupId") Long groupId);

    /**
     * Calculate total minutes late for a student
     */
    @Query("SELECT COALESCE(SUM(a.minutesLate), 0) FROM Attendance a " +
           "WHERE a.studentId = :studentId AND a.status = 'TARDANZA'")
    Integer calculateTotalMinutesLateForStudent(@Param("studentId") Long studentId);

    /**
     * Calculate average minutes late for a session
     */
    @Query("SELECT COALESCE(AVG(a.minutesLate), 0) FROM Attendance a " +
           "WHERE a.session.id = :sessionId AND a.status = 'TARDANZA'")
    Double calculateAverageMinutesLateForSession(@Param("sessionId") Long sessionId);



    // ==================== CLEANUP AND MAINTENANCE ====================

    /**
     * Delete all attendance records for a specific session
     * (Use with caution - typically only for test cleanup)
     */
    void deleteBySessionId(Long sessionId);

    /**
     * Delete all attendance records for a student
     * (Use with caution - GDPR compliance for right to be forgotten)
     */
    void deleteByStudentId(Long studentId);
}
