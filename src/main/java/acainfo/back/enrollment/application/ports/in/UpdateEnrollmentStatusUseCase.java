package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.domain.exception.EnrollmentNotFoundException;
import acainfo.back.enrollment.domain.exception.InvalidEnrollmentStatusException;

/**
 * Use case for updating enrollment status.
 */
public interface UpdateEnrollmentStatusUseCase {

    /**
     * Updates the status of an enrollment.
     *
     * Business rules:
     * - Enrollment must exist
     * - Status transition must be valid
     *
     * @param enrollmentId the enrollment ID
     * @param newStatus the new status
     * @param reason optional reason for status change
     * @throws EnrollmentNotFoundException if enrollment not found
     * @throws InvalidEnrollmentStatusException if status transition is invalid
     */
    void updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus newStatus, String reason);
}
