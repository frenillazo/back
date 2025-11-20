package acainfo.back.enrollment.application.ports.out;

import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.domain.model.AttendanceMode;

import java.util.List;
import java.util.Optional;

/**
 * Port for enrollment repository operations.
 * This interface defines the contract for enrollment persistence.
 */
public interface EnrollmentRepositoryPort {

    /**
     * Saves an enrollment (create or update).
     *
     * @param enrollment the enrollment to save
     * @return the saved enrollment
     */
    Enrollment save(Enrollment enrollment);

    /**
     * Finds an enrollment by ID.
     *
     * @param id the enrollment ID
     * @return Optional containing the enrollment if found
     */
    Optional<Enrollment> findById(Long id);

    /**
     * Finds all enrollments.
     *
     * @return list of all enrollments
     */
    List<Enrollment> findAll();

    /**
     * Finds enrollments by student ID.
     *
     * @param studentId the student ID
     * @return list of enrollments
     */
    List<Enrollment> findByStudentId(Long studentId);

    /**
     * Finds enrollments by student ID and status.
     *
     * @param studentId the student ID
     * @param status the enrollment status
     * @return list of enrollments
     */
    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    /**
     * Finds enrollments by subject group ID.
     *
     * @param groupId the subject group ID
     * @return list of enrollments
     */
    List<Enrollment> findBySubjectGroupId(Long groupId);

    /**
     * Finds enrollments by subject group ID and status.
     *
     * @param groupId the subject group ID
     * @param status the enrollment status
     * @return list of enrollments
     */
    List<Enrollment> findBySubjectGroupIdAndStatus(Long groupId, EnrollmentStatus status);

    /**
     * Finds enrollments in waiting queue for a group, ordered by enrollment date (FIFO).
     *
     * @param groupId the subject group ID
     * @param status the enrollment status (typically EN_ESPERA)
     * @return list of enrollments in waiting queue, ordered by enrollment date ascending
     */
    List<Enrollment> findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(Long groupId, EnrollmentStatus status);

    /**
     * Checks if a student is enrolled in a specific group with given status.
     *
     * @param studentId the student ID
     * @param groupId the subject group ID
     * @param status the enrollment status
     * @return true if enrolled with that status, false otherwise
     */
    boolean existsByStudentIdAndSubjectGroupIdAndStatus(Long studentId, Long groupId, EnrollmentStatus status);

    /**
     * Counts enrollments by student ID and status.
     *
     * @param studentId the student ID
     * @param status the enrollment status
     * @return count of enrollments
     */
    long countByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    /**
     * Counts enrollments by subject group ID and status.
     *
     * @param groupId the subject group ID
     * @param status the enrollment status
     * @return count of enrollments
     */
    long countBySubjectGroupIdAndStatus(Long groupId, EnrollmentStatus status);

    /**
     * Counts presential enrollments for a group.
     * These are enrollments with ACTIVO status and PRESENCIAL mode.
     *
     * @param groupId the subject group ID
     * @return count of presential enrollments
     */
    long countPresentialEnrollments(Long groupId);

    /**
     * Counts online enrollments for a group.
     * These are enrollments with ACTIVO status and ONLINE mode.
     *
     * @param groupId the subject group ID
     * @return count of online enrollments
     */
    long countOnlineEnrollments(Long groupId);

    /**
     * Finds all active enrollments for a student.
     *
     * @param studentId the student ID
     * @return list of active enrollments
     */
    List<Enrollment> findActiveEnrollmentsByStudentId(Long studentId);

    /**
     * Finds enrollments by attendance mode.
     *
     * @param groupId the subject group ID
     * @param mode the attendance mode
     * @return list of enrollments
     */
    List<Enrollment> findBySubjectGroupIdAndAttendanceMode(Long groupId, AttendanceMode mode);

    /**
     * Finds enrollment by student ID and group ID.
     *
     * @param studentId the student ID
     * @param groupId the subject group ID
     * @return Optional containing the enrollment if found
     */
    Optional<Enrollment> findByStudentIdAndSubjectGroupId(Long studentId, Long groupId);

    /**
     * Deletes an enrollment by ID.
     *
     * @param id the enrollment ID
     */
    void deleteById(Long id);

    /**
     * Checks if an enrollment exists by ID.
     *
     * @param id the enrollment ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Finds all enrollments by status.
     *
     * @param status the enrollment status
     * @return list of enrollments
     */
    List<Enrollment> findByStatus(EnrollmentStatus status);
}
