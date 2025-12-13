package com.acainfo.material.application.port.in;

import com.acainfo.material.application.dto.MaterialFilters;
import com.acainfo.material.domain.model.Material;
import com.acainfo.shared.application.dto.PageResponse;

import java.util.List;

/**
 * Use case for querying materials.
 * Input port defining the contract for material queries.
 *
 * <p>All users can see material metadata (name, description, file info).
 * Download access is controlled separately.</p>
 */
public interface GetMaterialUseCase {

    /**
     * Get material by ID.
     *
     * @param id Material ID
     * @return Material metadata
     * @throws com.acainfo.material.domain.exception.MaterialNotFoundException if not found
     */
    Material getById(Long id);

    /**
     * Find materials with dynamic filters.
     *
     * @param filters Filter criteria
     * @return Page of materials matching filters
     */
    PageResponse<Material> findWithFilters(MaterialFilters filters);

    /**
     * Get all materials for a subject.
     *
     * @param subjectId Subject ID
     * @return List of materials
     */
    List<Material> getBySubjectId(Long subjectId);

    /**
     * Check if user can download a material.
     *
     * @param materialId Material ID
     * @param userId User ID
     * @return true if user has download access
     */
    boolean canDownload(Long materialId, Long userId);
}
