package acainfo.back.enrollment.application.ports.out;

import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;

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
     * Finds all enrollments for a student.
     *
     * @param studentId the student ID
     * @return list of enrollments
     */
    List<Enrollment> findByStudentId(Long studentId);

    /**
     * Finds all enrollments for a subject group.
     *
     * @param subjectGroupId the subject group ID
     * @return list of enrollments
     */
    List<Enrollment> findBySubjectGroupId(Long subjectGroupId);

    /**
     * Finds all enrollments by status.
     *
     * @param status the enrollment status
     * @return list of enrollments
     */
    List<Enrollment> findByStatus(EnrollmentStatus status);

    /**
     * Finds all enrollments for a student with a specific status.
     *
     * @param studentId the student ID
     * @param status the enrollment status
     * @return list of enrollments
     */
    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    /**
     * Finds all enrollments for a subject group with a specific status.
     *
     * @param subjectGroupId the subject group ID
     * @param status the enrollment status
     * @return list of enrollments
     */
    List<Enrollment> findBySubjectGroupIdAndStatus(Long subjectGroupId, EnrollmentStatus status);

    /**
     * Finds an enrollment by student and subject group.
     *
     * @param studentId the student ID
     * @param subjectGroupId the subject group ID
     * @return Optional containing the enrollment if found
     */
    Optional<Enrollment> findByStudentIdAndSubjectGroupId(Long studentId, Long subjectGroupId);

    /**
     * Counts enrollments by status.
     *
     * @param status the enrollment status
     * @return count of enrollments
     */
    long countByStatus(EnrollmentStatus status);

    /**
     * Counts enrollments for a student.
     *
     * @param studentId the student ID
     * @return count of enrollments
     */
    long countByStudentId(Long studentId);

    /**
     * Counts active enrollments for a student.
     *
     * @param studentId the student ID
     * @return count of active enrollments
     */
    long countActiveByStudentId(Long studentId);

    /**
     * Counts enrollments for a subject group.
     *
     * @param subjectGroupId the subject group ID
     * @return count of enrollments
     */
    long countBySubjectGroupId(Long subjectGroupId);

    /**
     * Counts active enrollments for a subject group.
     *
     * @param subjectGroupId the subject group ID
     * @return count of active enrollments
     */
    long countActiveBySubjectGroupId(Long subjectGroupId);

    /**
     * Checks if an enrollment exists by student and subject group.
     *
     * @param studentId the student ID
     * @param subjectGroupId the subject group ID
     * @return true if exists, false otherwise
     */
    boolean existsByStudentIdAndSubjectGroupId(Long studentId, Long subjectGroupId);

    /**
     * Checks if an enrollment exists by ID.
     *
     * @param id the enrollment ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Deletes an enrollment by ID.
     *
     * @param id the enrollment ID
     */
    void deleteById(Long id);

    /**
     * Deletes an enrollment.
     *
     * @param enrollment the enrollment to delete
     */
    void delete(Enrollment enrollment);
}
