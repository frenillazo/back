package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.GroupRequest;

/**
 * Use case for creating a group request.
 */
public interface CreateGroupRequestUseCase {

    /**
     * Creates a new group request for a subject.
     * The requester is automatically added as the first supporter.
     *
     * @param subjectId the subject ID
     * @param requesterId the student ID who creates the request
     * @param comments optional comments about the request
     * @return the created group request
     */
    GroupRequest createGroupRequest(Long subjectId, Long requesterId, String comments);
}
