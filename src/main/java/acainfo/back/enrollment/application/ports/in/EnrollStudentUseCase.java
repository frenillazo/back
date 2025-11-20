package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.Enrollment;

/**
 * Use case for enrolling a student in a subject group.
 */
public interface EnrollStudentUseCase {

    /**
     * Enrolls a student in a subject group.
     *
     * @param studentId the student ID
     * @param groupId the subject group ID
     * @return the created enrollment
     */
    Enrollment enrollStudent(Long studentId, Long groupId);

    /**
     * Checks if a student can enroll in a group.
     *
     * @param studentId the student ID
     * @param groupId the subject group ID
     * @return true if can enroll, false otherwise
     */
    boolean canEnroll(Long studentId, Long groupId);
}
