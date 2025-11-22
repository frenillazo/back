package acainfo.back.material.application.ports.out;

import acainfo.back.material.domain.model.MaterialDomain;
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
    MaterialDomain save(MaterialDomain material);

    /**
     * Finds a material by ID.
     *
     * @param id the material ID
     * @return Optional containing the material if found
     */
    Optional<MaterialDomain> findById(Long id);

    /**
     * Finds all active materials for a subject group.
     *
     * @param subjectGroupId the subject group ID
     * @return list of active materials
     */
    List<MaterialDomain> findBySubjectGroupIdAndIsActiveTrue(Long subjectGroupId);

    /**
     * Finds all materials for a subject group (including inactive).
     *
     * @param subjectGroupId the subject group ID
     * @return list of all materials
     */
    List<MaterialDomain> findBySubjectGroupId(Long subjectGroupId);

    /**
     * Finds active materials by subject group and type.
     *
     * @param subjectGroupId the subject group ID
     * @param type the material type
     * @return list of materials
     */
    List<MaterialDomain> findBySubjectGroupIdAndTypeAndIsActiveTrue(Long subjectGroupId, MaterialType type);

    /**
     * Finds active materials by subject group and topic.
     *
     * @param subjectGroupId the subject group ID
     * @param topic the topic/unit
     * @return list of materials
     */
    List<MaterialDomain> findBySubjectGroupIdAndTopicAndIsActiveTrue(Long subjectGroupId, String topic);

    /**
     * Finds all materials uploaded by a user.
     *
     * @param uploaderId the uploader user ID
     * @return list of materials
     */
    List<MaterialDomain> findByUploadedById(Long uploaderId);

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
    List<MaterialDomain> findByTypeAndIsActiveTrue(MaterialType type);

    /**
     * Finds all distinct topics for a subject group
     */
    List<String> findDistinctTopicsBySubjectGroupId(Long subjectGroupId);

    /**
     * Checks if a file path already exists
     */
    boolean existsByFilePath(String filePath);

    /**
     * Calculate total storage size for a subject group
     */
    Long calculateTotalSizeBySubjectGroupId(Long subjectGroupId);
}
