package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a material folder is not found.
 */
public class MaterialFolderNotFoundException extends NotFoundException {

    public MaterialFolderNotFoundException(Long id) {
        super("Carpeta de materiales no encontrada con ID: " + id);
    }

    @Override
    public String getErrorCode() {
        return "MATERIAL_FOLDER_NOT_FOUND";
    }
}
