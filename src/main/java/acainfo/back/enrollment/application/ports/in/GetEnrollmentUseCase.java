package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.domain.exception.EnrollmentNotFoundException;

import java.util.List;

/**
 * Use case for retrieving enrollments.
 */
public interface GetEnrollmentUseCase {

    /**
     * Finds an enrollment by ID.
     *
     * @param id the enrollment ID
     * @return the enrollment
     * @throws EnrollmentNotFoundException if not found
     */
    Enrollment getEnrollmentById(Long id);

    /**
     * Finds all enrollments for a student.
     *
     * @param studentId the student ID
     * @return list of enrollments
     */
    List<Enrollment> getEnrollmentsByStudent(Long studentId);

    /**
     * Finds all enrollments for a subject group.
     *
     * @param subjectGroupId the subject group ID
     * @return list of enrollments
     */
    List<Enrollment> getEnrollmentsBySubjectGroup(Long subjectGroupId);

    /**
     * Finds all active enrollments for a student.
     *
     * @param studentId the student ID
     * @return list of active enrollments
     */
    List<Enrollment> getActiveEnrollmentsByStudent(Long studentId);

    /**
     * Finds all active enrollments for a subject group.
     *
     * @param subjectGroupId the subject group ID
     * @return list of active enrollments
     */
    List<Enrollment> getActiveEnrollmentsBySubjectGroup(Long subjectGroupId);

    /**
     * Finds all enrollments by status.
     *
     * @param status the enrollment status
     * @return list of enrollments
     */
    List<Enrollment> getEnrollmentsByStatus(EnrollmentStatus status);

    /**
     * Finds all enrollments.
     *
     * @return list of all enrollments
     */
    List<Enrollment> getAllEnrollments();

    /**
     * Counts active enrollments for a student.
     *
     * @param studentId the student ID
     * @return count of active enrollments
     */
    long countActiveEnrollmentsByStudent(Long studentId);
}
