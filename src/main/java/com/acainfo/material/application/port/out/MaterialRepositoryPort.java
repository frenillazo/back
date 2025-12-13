package com.acainfo.material.application.port.out;

import com.acainfo.material.application.dto.MaterialFilters;
import com.acainfo.material.domain.model.Material;
import org.springframework.data.domain.Page;

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
}
