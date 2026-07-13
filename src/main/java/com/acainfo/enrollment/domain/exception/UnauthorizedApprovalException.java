package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a user attempts to approve/reject an enrollment
 * without proper authorization (not the group's teacher or an admin).
 */
public class UnauthorizedApprovalException extends BusinessRuleException {

    public UnauthorizedApprovalException(Long userId, Long courseId) {
        super(String.format("El usuario %d no está autorizado para aprobar/rechazar inscripciones del grupo %d", userId, courseId));
    }

    @Override
    public String getErrorCode() {
        return "UNAUTHORIZED_APPROVAL";
    }
}
