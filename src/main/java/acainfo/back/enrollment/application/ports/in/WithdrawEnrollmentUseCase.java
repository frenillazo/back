package acainfo.back.enrollment.application.ports.in;

/**
 * Use case for withdrawing from an enrollment.
 */
public interface WithdrawEnrollmentUseCase {

    /**
     * Withdraws a student from a group.
     *
     * @param enrollmentId the enrollment ID
     * @param reason the withdrawal reason
     */
    void withdrawEnrollment(Long enrollmentId, String reason);
}
