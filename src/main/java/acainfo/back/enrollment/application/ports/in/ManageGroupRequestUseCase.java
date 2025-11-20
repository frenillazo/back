package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.GroupRequest;

/**
 * Use case for managing group requests (approve/reject).
 * These operations are typically performed by administrators.
 */
public interface ManageGroupRequestUseCase {

    /**
     * Approves a group request.
     * This should trigger the creation of a new subject group.
     *
     * @param requestId the group request ID
     * @param adminId the admin ID who approves
     * @return the approved group request
     */
    GroupRequest approveRequest(Long requestId, Long adminId);

    /**
     * Rejects a group request with a reason.
     *
     * @param requestId the group request ID
     * @param adminId the admin ID who rejects
     * @param reason the rejection reason
     * @return the rejected group request
     */
    GroupRequest rejectRequest(Long requestId, Long adminId, String reason);
}
