package acainfo.back.material.application.ports.out;

import acainfo.back.material.domain.model.Material;
import acainfo.back.material.domain.model.MaterialType;

import java.util.List;
import java.util.Optional;

/**
 * Port for material repository operations.
 * Defines the contract for material data access.
 */
public interface MaterialRepositoryPort {

    /**
     * Saves a material.
     *
     * @param material the material to save
     * @return the saved material
     */
    Material save(Material material);

    /**
     * Finds a material by ID.
     *
     * @param id the material ID
     * @return Optional containing the material if found
     */
    Optional<Material> findById(Long id);

    /**
     * Finds all active materials for a subject group.
     *
     * @param subjectGroupId the subject group ID
     * @return list of active materials
     */
    List<Material> findBySubjectGroupIdAndIsActiveTrue(Long subjectGroupId);

    /**
     * Finds all materials for a subject group (including inactive).
     *
     * @param subjectGroupId the subject group ID
     * @return list of all materials
     */
    List<Material> findBySubjectGroupId(Long subjectGroupId);

    /**
     * Finds active materials by subject group and type.
     *
     * @param subjectGroupId the subject group ID
     * @param type the material type
     * @return list of materials
     */
    List<Material> findBySubjectGroupIdAndTypeAndIsActiveTrue(Long subjectGroupId, MaterialType type);

    /**
     * Finds active materials by subject group and topic.
     *
     * @param subjectGroupId the subject group ID
     * @param topic the topic/unit
     * @return list of materials
     */
    List<Material> findBySubjectGroupIdAndTopicAndIsActiveTrue(Long subjectGroupId, String topic);

    /**
     * Finds all materials uploaded by a user.
     *
     * @param uploaderId the uploader user ID
     * @return list of materials
     */
    List<Material> findByUploadedById(Long uploaderId);

    /**
     * Checks if a material exists and is active.
     *
     * @param id the material ID
     * @return true if exists and is active
     */
    boolean existsByIdAndIsActiveTrue(Long id);

    /**
     * Deletes a material (physical delete - use with caution).
     *
     * @param id the material ID
     */
    void deleteById(Long id);

    /**
     * Counts active materials for a subject group.
     *
     * @param subjectGroupId the subject group ID
     * @return count of active materials
     */
    long countBySubjectGroupIdAndIsActiveTrue(Long subjectGroupId);

    /**
     * Finds all materials by type.
     *
     * @param type the material type
     * @return list of materials
     */
    List<Material> findByTypeAndIsActiveTrue(MaterialType type);
}
