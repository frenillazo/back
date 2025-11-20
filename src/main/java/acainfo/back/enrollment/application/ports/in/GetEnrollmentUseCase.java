package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.Enrollment;

import java.util.List;

/**
 * Use case for retrieving enrollment information.
 */
public interface GetEnrollmentUseCase {

    /**
     * Gets an enrollment by ID.
     *
     * @param id the enrollment ID
     * @return the enrollment
     */
    Enrollment getEnrollmentById(Long id);

    /**
     * Gets all active enrollments for a student.
     *
     * @param studentId the student ID
     * @return list of active enrollments
     */
    List<Enrollment> getActiveEnrollmentsByStudent(Long studentId);

    /**
     * Gets all enrollments for a student.
     *
     * @param studentId the student ID
     * @return list of enrollments
     */
    List<Enrollment> getAllEnrollmentsByStudent(Long studentId);

    /**
     * Gets all enrollments for a subject group.
     *
     * @param groupId the subject group ID
     * @return list of enrollments
     */
    List<Enrollment> getEnrollmentsByGroup(Long groupId);

    /**
     * Gets all active enrollments for a subject group.
     *
     * @param groupId the subject group ID
     * @return list of active enrollments
     */
    List<Enrollment> getActiveEnrollmentsByGroup(Long groupId);

    /**
     * Checks if a student is enrolled in a group.
     *
     * @param studentId the student ID
     * @param groupId the subject group ID
     * @return true if enrolled, false otherwise
     */
    boolean isStudentEnrolled(Long studentId, Long groupId);
}
