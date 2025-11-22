package acainfo.back.attendance.application.ports.out;

import acainfo.back.attendance.domain.model.AttendanceDomain;
import acainfo.back.attendance.domain.model.AttendanceStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Port for attendance repository operations.
 * This interface defines the contract for attendance persistence.
 */
public interface AttendanceRepositoryPort {

    /**
     * Saves an attendance (create or update).
     */
    AttendanceDomain save(AttendanceDomain attendance);

    /**
     * Finds an attendance by ID
     */
    Optional<AttendanceDomain> findById(Long id);

    /**
     * Finds all attendance records
     */
    List<AttendanceDomain> findAll();

    /**
     * Finds all attendance records for a session, ordered by student name
     */
    List<AttendanceDomain> findBySessionId(Long sessionId);

    /**
     * Finds all attendance records for an enrollment
     */
    List<AttendanceDomain> findByEnrollmentId(Long enrollmentId);

    /**
     * Finds all attendance records for a student (across all enrollments)
     */
    List<AttendanceDomain> findByStudentId(Long studentId);

    /**
     * Finds attendance for a specific enrollment in a specific session
     */
    Optional<AttendanceDomain> findBySessionIdAndEnrollmentId(Long sessionId, Long enrollmentId);

    /**
     * Finds all attendance records for a subject group
     */
    List<AttendanceDomain> findByGroupId(Long groupId);

    /**
     * Finds attendance records for an enrollment with specific status
     */
    List<AttendanceDomain> findByEnrollmentIdAndStatus(Long enrollmentId, AttendanceStatus status);

    /**
     * Finds attendance records for a student with specific status (across all enrollments)
     */
    List<AttendanceDomain> findByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    /**
     * Finds attendance records for a session with specific status
     */
    List<AttendanceDomain> findBySessionIdAndStatus(Long sessionId, AttendanceStatus status);

    /**
     * Finds all absences (AUSENTE or JUSTIFICADO) for a student
     */
    List<AttendanceDomain> findAbsencesByStudentId(Long studentId);

    /**
     * Finds all effective attendance (PRESENTE or TARDANZA) for a student
     */
    List<AttendanceDomain> findEffectiveAttendanceByStudentId(Long studentId);

    /**
     * Finds attendance records for an enrollment within a date range
     */
    List<AttendanceDomain> findByEnrollmentIdAndDateRange(
        Long enrollmentId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Finds attendance records for a student within a date range (across all enrollments)
     */
    List<AttendanceDomain> findByStudentIdAndDateRange(
        Long studentId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Finds attendance records for a group within a date range
     */
    List<AttendanceDomain> findByGroupIdAndDateRange(
        Long groupId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Checks if attendance exists for an enrollment in a session
     */
    boolean existsBySessionIdAndEnrollmentId(Long sessionId, Long enrollmentId);

    /**
     * Checks if any attendance has been recorded for a session
     */
    boolean existsBySessionId(Long sessionId);

    /**
     * Checks if an enrollment has any attendance records
     */
    boolean existsByEnrollmentId(Long enrollmentId);

    /**
     * Counts attendance records for a session
     */
    long countBySessionId(Long sessionId);

    /**
     * Counts attendance records for an enrollment
     */
    long countByEnrollmentId(Long enrollmentId);

    /**
     * Counts attendance records for a student (across all enrollments)
     */
    long countByStudentId(Long studentId);

    /**
     * Counts attendance records by status for an enrollment
     */
    long countByEnrollmentIdAndStatus(Long enrollmentId, AttendanceStatus status);

    /**
     * Counts attendance records by status for a student (across all enrollments)
     */
    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    /**
     * Counts effective attendance (PRESENTE + TARDANZA) for an enrollment
     */
    long countEffectiveAttendanceByEnrollmentId(Long enrollmentId);

    /**
     * Counts absences (AUSENTE + JUSTIFICADO) for an enrollment
     */
    long countAbsencesByEnrollmentId(Long enrollmentId);

    /**
     * Counts attendance by status for an enrollment
     * Returns pairs of [status, count]
     */
    List<Object[]> countByStatusForEnrollment(Long enrollmentId);

    /**
     * Counts attendance by status for a student (across all enrollments)
     * Returns pairs of [status, count]
     */
    List<Object[]> countByStatusForStudent(Long studentId);

    /**
     * Counts attendance by status for a session
     * Returns pairs of [status, count]
     */
    List<Object[]> countByStatusForSession(Long sessionId);

    /**
     * Counts attendance by status for a subject group
     * Returns pairs of [status, count]
     */
    List<Object[]> countByStatusForGroup(Long groupId);

    /**
     * Calculates attendance rate for an enrollment
     * Returns percentage of effective attendance (PRESENTE + TARDANZA)
     */
    Double calculateAttendanceRateForEnrollment(Long enrollmentId);

    /**
     * Calculates attendance rate for a student (across all enrollments)
     * Returns percentage of effective attendance (PRESENTE + TARDANZA)
     */
    Double calculateAttendanceRateForStudent(Long studentId);

    /**
     * Calculates attendance rate for a student in a specific group
     */
    Double calculateAttendanceRateForStudentInGroup(Long studentId, Long groupId);

    /**
     * Calculates average attendance rate for a session
     */
    Double calculateAttendanceRateForSession(Long sessionId);

    /**
     * Calculates average attendance rate for a group
     */
    Double calculateAttendanceRateForGroup(Long groupId);

    /**
     * Calculates total minutes late for an enrollment
     */
    Integer calculateTotalMinutesLateForEnrollment(Long enrollmentId);

    /**
     * Calculates total minutes late for a student (across all enrollments)
     */
    Integer calculateTotalMinutesLateForStudent(Long studentId);

    /**
     * Deletes an attendance by ID
     */
    void deleteById(Long id);

    /**
     * Deletes all attendance records for a session
     */
    void deleteBySessionId(Long sessionId);

    /**
     * Deletes all attendance records for an enrollment
     */
    void deleteByEnrollmentId(Long enrollmentId);
}
