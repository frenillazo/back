package acainfo.back.material.domain.exception;

import acainfo.back.config.exception.DomainException;

/**
 * Exception thrown when a user tries to access material without proper authorization.
 * Access requires: active enrollment + valid payment status (if material requires payment).
 */
public class UnauthorizedMaterialAccessException extends DomainException {

    public UnauthorizedMaterialAccessException(Long studentId, Long materialId) {
        super("Student " + studentId + " is not authorized to access material " + materialId +
            ". Requires active enrollment and valid payment status.");
    }

    public UnauthorizedMaterialAccessException(String message) {
        super(message);
    }

    public UnauthorizedMaterialAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
