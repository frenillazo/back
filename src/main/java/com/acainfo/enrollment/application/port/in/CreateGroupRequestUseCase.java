package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.application.dto.CreateGroupRequestCommand;
import com.acainfo.enrollment.domain.model.GroupRequest;

/**
 * Use case for creating group requests.
 * Input port defining the contract for group request creation.
 *
 * <p>Students can request the creation of a new group for a subject.
 * The requester is automatically added as the first supporter.</p>
 */
public interface CreateGroupRequestUseCase {

    /**
     * Create a new group request.
     * The requester is automatically added as the first supporter.
     *
     * @param command Group request data
     * @return The created group request with PENDING status
     * @throws com.acainfo.subject.domain.exception.SubjectNotFoundException if subject not found
     * @throws com.acainfo.user.domain.exception.UserNotFoundException if requester not found
     */
    GroupRequest create(CreateGroupRequestCommand command);
}
