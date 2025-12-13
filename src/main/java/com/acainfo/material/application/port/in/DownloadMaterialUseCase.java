package com.acainfo.material.application.port.in;

import com.acainfo.material.application.dto.MaterialDownload;

/**
 * Use case for downloading material content.
 * Input port defining the contract for material download.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Admins and teachers can always download</li>
 *   <li>Students need active enrollment in a group of the subject</li>
 *   <li>Students need payments up to date (no overdue payments)</li>
 * </ul>
 */
public interface DownloadMaterialUseCase {

    /**
     * Download material content with access control.
     *
     * @param materialId Material ID
     * @param userId User requesting download
     * @return Material download data (content + metadata)
     * @throws com.acainfo.material.domain.exception.MaterialNotFoundException if material not found
     * @throws com.acainfo.material.domain.exception.MaterialAccessDeniedException if user cannot access
     * @throws com.acainfo.material.domain.exception.FileStorageException if file retrieval fails
     */
    MaterialDownload download(Long materialId, Long userId);
}
