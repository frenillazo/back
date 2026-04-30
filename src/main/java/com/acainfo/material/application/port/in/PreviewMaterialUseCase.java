package com.acainfo.material.application.port.in;

import com.acainfo.material.application.dto.MaterialDownload;

/**
 * Use case for in-browser preview of material content.
 *
 * <p>Same access rules as {@link DownloadMaterialUseCase} EXCEPT it does NOT
 * check the {@code downloadDisabled} flag: a material can be hidden from
 * direct download but still previewable in the in-app viewer.</p>
 *
 * <p>Access rules:</p>
 * <ul>
 *   <li>Admins and teachers can always preview</li>
 *   <li>For non-admin users the material must be {@code visible}</li>
 *   <li>Students need active enrollment + payments up to date</li>
 * </ul>
 */
public interface PreviewMaterialUseCase {

    /**
     * Stream material content for in-browser visualization.
     *
     * @throws com.acainfo.material.domain.exception.MaterialNotFoundException if not found
     * @throws com.acainfo.material.domain.exception.MaterialAccessDeniedException if access denied
     */
    MaterialDownload preview(Long materialId, Long userId);
}
