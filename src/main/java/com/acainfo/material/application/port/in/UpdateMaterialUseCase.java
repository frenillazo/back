package com.acainfo.material.application.port.in;

import com.acainfo.material.application.dto.UpdateMaterialCommand;
import com.acainfo.material.domain.model.Material;

import java.util.List;

/**
 * Use case for administrative material updates: editing metadata
 * and toggling visibility / download in single or batch mode.
 */
public interface UpdateMaterialUseCase {

    /**
     * Update metadata (name, description) and/or visibility/download flags of a material.
     * Null fields in the command are ignored.
     */
    Material updateMetadata(Long materialId, UpdateMaterialCommand command);

    /**
     * Batch toggle the downloadDisabled flag for multiple materials.
     *
     * @return number of materials updated
     */
    int batchSetDownloadDisabled(List<Long> ids, boolean disabled);

    /**
     * Batch toggle visibility for multiple materials.
     *
     * @return number of materials updated
     */
    int batchSetVisibility(List<Long> ids, boolean visible);
}
