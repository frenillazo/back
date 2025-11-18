package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.exception.EnrollmentAlreadyExistsException;
import acainfo.back.subjectgroup.domain.exception.GroupFullException;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.shared.domain.exception.UserNotFoundException;

/**
 * Use case for creating a new enrollment.
 */
public interface CreateEnrollmentUseCase {

    /**
     * Creates a new enrollment for a student in a subject group.
     *
     * Business rules:
     * - Student must exist and have STUDENT role
     * - Subject group must exist and be active
     * - Subject group must have available places (unless online attendance is allowed)
     * - Student cannot be already enrolled in the same group
     * - If student has 2+ enrollments, online attendance can be allowed
     *
     * @param enrollment the enrollment to create
     * @return the created enrollment with generated ID
     * @throws UserNotFoundException if student not found
     * @throws GroupNotFoundException if subject group not found
     * @throws EnrollmentAlreadyExistsException if student already enrolled
     * @throws GroupFullException if group is full and online attendance not allowed
     */
    Enrollment createEnrollment(Enrollment enrollment);
}
