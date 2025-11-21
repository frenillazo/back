package acainfo.back.material.domain.exception;

import acainfo.back.shared.domain.exception.DomainException;

/**
 * Exception thrown when a material is not found.
 */
public class MaterialNotFoundException extends DomainException {

    public MaterialNotFoundException(Long id) {
        super("Material not found with id: " + id);
    }

    public MaterialNotFoundException(String message) {
        super(message);
    }

    public MaterialNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
