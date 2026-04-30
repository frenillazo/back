package com.acainfo.material.application.port.out;

import com.acainfo.material.application.dto.MaterialFilters;
import com.acainfo.material.domain.model.Material;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Output port for Material persistence.
 * Defines the contract for material repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface MaterialRepositoryPort {

    /**
     * Save or update a material.
     *
     * @param material Domain material to persist
     * @return Persisted material with ID
     */
    Material save(Material material);

    /**
     * Find material by ID.
     *
     * @param id Material ID
     * @return Optional containing the material if found
     */
    Optional<Material> findById(Long id);

    /**
     * Find materials with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of materials matching filters
     */
    Page<Material> findWithFilters(MaterialFilters filters);

    /**
     * Find all materials for a subject.
     *
     * @param subjectId Subject ID
     * @return List of materials ordered by uploadedAt desc
     */
    List<Material> findBySubjectId(Long subjectId);

    /**
     * Find all materials uploaded by a user.
     *
     * @param uploadedById User ID
     * @return List of materials
     */
    List<Material> findByUploadedById(Long uploadedById);

    /**
     * Check if material exists by ID.
     *
     * @param id Material ID
     * @return true if exists
     */
    boolean existsById(Long id);

    /**
     * Delete a material by ID.
     *
     * @param id Material ID
     */
    void delete(Long id);

    /**
     * Find recent materials for given subjects uploaded within specified days.
     *
     * @param subjectIds List of subject IDs to filter by
     * @param days Number of days to look back
     * @return List of materials ordered by uploadedAt desc
     */
    List<Material> findRecentBySubjectIds(List<Long> subjectIds, int days);

    /**
     * Find multiple materials by IDs (used by batch operations to validate existence).
     */
    List<Material> findAllByIds(List<Long> ids);

    /**
     * Batch update the downloadDisabled flag for the given material IDs.
     * When {@code disabled=false}, also sets {@code downloadEnabledAt} to the provided timestamp
     * (so the auto-disable scheduled task counts from the last reactivation).
     *
     * @param ids        material IDs to update
     * @param disabled   new value for downloadDisabled
     * @param enabledAt  timestamp to store as downloadEnabledAt when disabled=false (ignored otherwise)
     * @return number of rows affected
     */
    int batchUpdateDownloadDisabled(List<Long> ids, boolean disabled, LocalDateTime enabledAt);

    /**
     * Batch update the visible flag for the given material IDs.
     * When {@code visible=true}, also sets {@code visibilityEnabledAt} to the provided timestamp.
     *
     * @param ids        material IDs to update
     * @param visible    new value for visible
     * @param enabledAt  timestamp to store as visibilityEnabledAt when visible=true (ignored otherwise)
     * @return number of rows affected
     */
    int batchUpdateVisibility(List<Long> ids, boolean visible, LocalDateTime enabledAt);

    /**
     * Find materials that are currently visible AND with download enabled, where both flags
     * have been active for at least {@code daysThreshold} days. Used by the auto-disable
     * scheduled task to enforce periodic admin review.
     */
    List<Material> findExpiredActiveMaterials(int daysThreshold);
}
