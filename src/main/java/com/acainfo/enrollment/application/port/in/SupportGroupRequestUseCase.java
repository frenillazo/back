package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.domain.model.GroupRequest;

/**
 * Use case for supporting group requests.
 * Input port defining the contract for adding supporters to a request.
 *
 * <p>Minimum 8 supporters are required for admin to approve the request.</p>
 */
public interface SupportGroupRequestUseCase {

    /**
     * Add a supporter to a group request.
     *
     * @param groupRequestId Group request ID
     * @param studentId Student ID to add as supporter
     * @return The updated group request
     * @throws com.acainfo.enrollment.domain.exception.GroupRequestNotFoundException if request not found
     * @throws com.acainfo.enrollment.domain.exception.InvalidGroupRequestStateException if not pending
     * @throws com.acainfo.enrollment.domain.exception.AlreadySupporterException if already a supporter
     */
    GroupRequest addSupporter(Long groupRequestId, Long studentId);

    /**
     * Remove a supporter from a group request.
     *
     * @param groupRequestId Group request ID
     * @param studentId Student ID to remove
     * @return The updated group request
     * @throws com.acainfo.enrollment.domain.exception.GroupRequestNotFoundException if request not found
     * @throws com.acainfo.enrollment.domain.exception.InvalidGroupRequestStateException if not pending
     */
    GroupRequest removeSupporter(Long groupRequestId, Long studentId);
}
