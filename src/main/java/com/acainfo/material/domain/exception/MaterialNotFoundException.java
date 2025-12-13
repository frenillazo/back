package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a material is not found.
 */
public class MaterialNotFoundException extends NotFoundException {

    public MaterialNotFoundException(Long id) {
        super("Material not found with ID: " + id);
    }

    @Override
    public String getErrorCode() {
        return "MATERIAL_NOT_FOUND";
    }
}
