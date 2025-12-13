package com.acainfo.material.application.port.in;

import com.acainfo.material.application.dto.UploadMaterialCommand;
import com.acainfo.material.domain.model.Material;

/**
 * Use case for uploading material to a subject.
 * Input port defining the contract for material upload.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Only admins and teachers can upload materials</li>
 *   <li>File type must be in the whitelist</li>
 *   <li>Material is associated with a subject (shared across groups)</li>
 * </ul>
 */
public interface UploadMaterialUseCase {

    /**
     * Upload a material file for a subject.
     *
     * @param command Upload data including file content
     * @return The created material with metadata
     * @throws com.acainfo.material.domain.exception.InvalidFileTypeException if file type not allowed
     * @throws com.acainfo.material.domain.exception.FileStorageException if storage fails
     * @throws com.acainfo.subject.domain.exception.SubjectNotFoundException if subject not found
     */
    Material upload(UploadMaterialCommand command);
}
