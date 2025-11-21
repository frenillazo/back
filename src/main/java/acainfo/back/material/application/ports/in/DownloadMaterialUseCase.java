package acainfo.back.material.application.ports.in;

import org.springframework.core.io.Resource;

/**
 * Use case for downloading educational material files.
 * Access control is enforced based on enrollment and payment status.
 */
public interface DownloadMaterialUseCase {

    /**
     * Downloads a material file.
     * Validates that the user has access to the material.
     *
     * @param materialId the material ID
     * @param userId the user ID requesting the download
     * @return the file as a Resource
     * @throws acainfo.back.material.domain.exception.MaterialNotFoundException if material doesn't exist
     * @throws acainfo.back.material.domain.exception.UnauthorizedMaterialAccessException if user doesn't have access
     * @throws acainfo.back.material.domain.exception.FileStorageException if file cannot be read
     */
    Resource downloadMaterial(Long materialId, Long userId);

    /**
     * Gets the file name for a material (for Content-Disposition header).
     *
     * @param materialId the material ID
     * @return the original file name
     */
    String getFileName(Long materialId);
}
