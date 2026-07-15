package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when an AI LaTeX job is not found.
 */
public class MaterialAiJobNotFoundException extends NotFoundException {

    public MaterialAiJobNotFoundException(Long id) {
        super("Job de generación no encontrado con ID: " + id);
    }

    @Override
    public String getErrorCode() {
        return "MATERIAL_AI_JOB_NOT_FOUND";
    }
}
