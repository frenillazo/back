package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.exception.EnrollmentNotFoundException;
import acainfo.back.enrollment.domain.exception.EnrollmentCannotBeCancelledException;

/**
 * Use case for cancelling an enrollment.
 */
public interface CancelEnrollmentUseCase {

    /**
     * Cancels an enrollment.
     *
     * Business rules:
     * - Enrollment must exist
     * - Enrollment must be in ACTIVE or PENDING status
     * - Group occupancy is decremented
     *
     * @param enrollmentId the ID of the enrollment to cancel
     * @param reason the reason for cancellation
     * @throws EnrollmentNotFoundException if enrollment not found
     * @throws EnrollmentCannotBeCancelledException if enrollment cannot be cancelled
     */
    void cancelEnrollment(Long enrollmentId, String reason);
}
