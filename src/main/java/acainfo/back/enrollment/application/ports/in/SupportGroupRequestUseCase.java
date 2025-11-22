package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.GroupRequestDomain;

/**
 * Use case for supporting a group request.
 */
public interface SupportGroupRequestUseCase {

    /**
     * Adds a student as a supporter of a group request.
     *
     * @param requestId the group request ID
     * @param studentId the student ID
     * @return the updated group request
     */
    GroupRequestDomain supportRequest(Long requestId, Long studentId);

    /**
     * Removes a student's support from a group request.
     *
     * @param requestId the group request ID
     * @param studentId the student ID
     * @return the updated group request
     */
    GroupRequestDomain unsupportRequest(Long requestId, Long studentId);
}
