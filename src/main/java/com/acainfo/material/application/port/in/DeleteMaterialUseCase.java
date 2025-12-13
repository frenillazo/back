package com.acainfo.material.application.port.in;

/**
 * Use case for deleting material.
 * Input port defining the contract for material deletion.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Only admins and teachers can delete materials</li>
 *   <li>Deletes both metadata and stored file</li>
 * </ul>
 */
public interface DeleteMaterialUseCase {

    /**
     * Delete a material and its stored file.
     *
     * @param materialId Material ID to delete
     * @throws com.acainfo.material.domain.exception.MaterialNotFoundException if material not found
     * @throws com.acainfo.material.domain.exception.FileStorageException if file deletion fails
     */
    void delete(Long materialId);
}
