package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a user attempts to approve/reject an enrollment
 * without proper authorization (not the group's teacher or an admin).
 */
public class UnauthorizedApprovalException extends BusinessRuleException {

    public UnauthorizedApprovalException(Long userId, Long groupId) {
        super(String.format("User %d is not authorized to approve/reject enrollments for group %d", userId, groupId));
    }

    @Override
    public String getErrorCode() {
        return "UNAUTHORIZED_APPROVAL";
    }
}
