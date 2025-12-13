package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a user tries to access material content without permission.
 * Students need active enrollment and payments up to date.
 */
public class MaterialAccessDeniedException extends BusinessRuleException {

    public MaterialAccessDeniedException(Long materialId, Long userId) {
        super("Access denied to material " + materialId + " for user " + userId);
    }

    public MaterialAccessDeniedException(Long materialId, Long userId, String reason) {
        super("Access denied to material " + materialId + " for user " + userId + ": " + reason);
    }

    @Override
    public String getErrorCode() {
        return "MATERIAL_ACCESS_DENIED";
    }
}
