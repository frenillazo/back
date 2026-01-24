package com.acainfo.material.application.port.out;

import com.acainfo.material.domain.model.MaterialCategory;

import java.io.InputStream;

/**
 * Output port for file storage operations.
 * Defines the contract for storing and retrieving file content.
 *
 * <p>Implementations:</p>
 * <ul>
 *   <li>LocalFileStorageAdapter - Development (local filesystem)</li>
 *   <li>S3FileStorageAdapter - Production (AWS S3 or compatible)</li>
 * </ul>
 */
public interface FileStoragePort {

    /**
     * Store a file and return the storage path.
     *
     * @param content File content as InputStream
     * @param storedFilename UUID-based filename to store
     * @param subjectId Subject ID for organizing files
     * @param category Material category for organizing files
     * @return Storage path relative to base directory
     * @throws com.acainfo.material.domain.exception.FileStorageException if storage fails
     */
    String store(InputStream content, String storedFilename, Long subjectId, MaterialCategory category);

    /**
     * Retrieve file content.
     *
     * @param storagePath Path returned by store()
     * @return File content as InputStream
     * @throws com.acainfo.material.domain.exception.FileStorageException if retrieval fails
     */
    InputStream retrieve(String storagePath);

    /**
     * Delete a stored file.
     *
     * @param storagePath Path returned by store()
     * @throws com.acainfo.material.domain.exception.FileStorageException if deletion fails
     */
    void delete(String storagePath);

    /**
     * Check if a file exists.
     *
     * @param storagePath Path to check
     * @return true if file exists
     */
    boolean exists(String storagePath);
}
