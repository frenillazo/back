package acainfo.back.material.infrastructure.adapters.out.persistence;

import acainfo.back.material.domain.model.Material;
import acainfo.back.material.domain.model.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Material entity.
 * Spring Data JPA automatically implements these methods.
 */
@Repository
public interface JpaMaterialRepository extends JpaRepository<Material, Long> {

    /**
     * Finds all active materials for a subject group.
     */
    List<Material> findBySubjectGroupIdAndIsActiveTrue(Long subjectGroupId);

    /**
     * Finds all materials for a subject group (including inactive).
     */
    List<Material> findBySubjectGroupId(Long subjectGroupId);

    /**
     * Finds active materials by subject group and type.
     */
    List<Material> findBySubjectGroupIdAndTypeAndIsActiveTrue(Long subjectGroupId, MaterialType type);

    /**
     * Finds active materials by subject group and topic.
     */
    List<Material> findBySubjectGroupIdAndTopicAndIsActiveTrue(Long subjectGroupId, String topic);

    /**
     * Finds all materials uploaded by a user.
     */
    List<Material> findByUploadedById(Long uploaderId);

    /**
     * Checks if a material exists and is active.
     */
    boolean existsByIdAndIsActiveTrue(Long id);

    /**
     * Counts active materials for a subject group.
     */
    long countBySubjectGroupIdAndIsActiveTrue(Long subjectGroupId);

    /**
     * Finds all active materials by type.
     */
    List<Material> findByTypeAndIsActiveTrue(MaterialType type);
}
