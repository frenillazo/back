package com.acainfo.material.application.dto;

import com.acainfo.material.domain.model.MaterialCategory;
import java.io.InputStream;

/**
 * Command to upload a material file.
 *
 * @param subjectId Subject ID to associate the material
 * @param uploadedById User ID who is uploading (teacher or admin)
 * @param name Display name for the material
 * @param description Optional description
 * @param originalFilename Original filename as uploaded
 * @param mimeType MIME type of the file
 * @param fileSize File size in bytes
 * @param content File content as InputStream
 * @param category Material category (defaults to OTROS if not provided)
 */
public record UploadMaterialCommand(
        Long subjectId,
        Long uploadedById,
        String name,
        String description,
        String originalFilename,
        String mimeType,
        Long fileSize,
        InputStream content,
        MaterialCategory category
) {
}
