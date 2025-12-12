package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.application.dto.ProcessGroupRequestCommand;
import com.acainfo.enrollment.domain.model.GroupRequest;

/**
 * Use case for admin processing of group requests.
 * Input port defining the contract for approving/rejecting requests.
 */
public interface ProcessGroupRequestUseCase {

    /**
     * Approve a group request and create the new group.
     * Requires minimum 8 supporters.
     *
     * @param command Process command with admin info
     * @return The approved group request with createdGroupId set
     * @throws com.acainfo.enrollment.domain.exception.GroupRequestNotFoundException if request not found
     * @throws com.acainfo.enrollment.domain.exception.InvalidGroupRequestStateException if not pending
     * @throws com.acainfo.enrollment.domain.exception.InsufficientSupportersException if less than 8 supporters
     */
    GroupRequest approve(ProcessGroupRequestCommand command);

    /**
     * Reject a group request.
     *
     * @param command Process command with admin info and reason
     * @return The rejected group request
     * @throws com.acainfo.enrollment.domain.exception.GroupRequestNotFoundException if request not found
     * @throws com.acainfo.enrollment.domain.exception.InvalidGroupRequestStateException if not pending
     */
    GroupRequest reject(ProcessGroupRequestCommand command);
}
