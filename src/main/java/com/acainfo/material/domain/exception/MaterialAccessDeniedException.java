package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a user tries to access material content without permission.
 * Students need active enrollment and payments up to date.
 */
public class MaterialAccessDeniedException extends BusinessRuleException {

    public MaterialAccessDeniedException(Long materialId, Long userId) {
        super("Acceso denegado al material " + materialId + " para el usuario " + userId);
    }

    public MaterialAccessDeniedException(Long materialId, Long userId, String reason) {
        super("Acceso denegado al material " + materialId + " para el usuario " + userId + ": " + reason);
    }

    @Override
    public String getErrorCode() {
        return "MATERIAL_ACCESS_DENIED";
    }
}
