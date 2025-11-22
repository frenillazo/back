package acainfo.back.material.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.material.domain.model.MaterialType;
import acainfo.back.material.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for Material persistence
 * Works with JPA entities only
 */
@Repository
public interface MaterialJpaRepository extends JpaRepository<MaterialJpaEntity, Long> {

    /**
     * Find all materials for a subject group
     */
    @Query("SELECT m FROM Material m WHERE m.subjectGroup.id = :groupId ORDER BY m.uploadedAt DESC")
    List<MaterialJpaEntity> findBySubjectGroupId(@Param("groupId") Long groupId);

    /**
     * Find active materials for a subject group
     */
    @Query("SELECT m FROM Material m WHERE m.subjectGroup.id = :groupId AND m.isActive = true ORDER BY m.uploadedAt DESC")
    List<MaterialJpaEntity> findActiveBySubjectGroupId(@Param("groupId") Long groupId);

    /**
     * Find materials by type
     */
    @Query("SELECT m FROM Material m WHERE m.type = :type AND m.isActive = true ORDER BY m.uploadedAt DESC")
    List<MaterialJpaEntity> findByType(@Param("type") MaterialType type);

    /**
     * Find materials by topic
     */
    @Query("SELECT m FROM Material m WHERE m.subjectGroup.id = :groupId AND m.topic = :topic AND m.isActive = true ORDER BY m.uploadedAt DESC")
    List<MaterialJpaEntity> findBySubjectGroupIdAndTopic(
            @Param("groupId") Long groupId,
            @Param("topic") String topic
    );

    /**
     * Find materials uploaded by a specific user
     */
    @Query("SELECT m FROM Material m WHERE m.uploadedBy.id = :userId ORDER BY m.uploadedAt DESC")
    List<MaterialJpaEntity> findByUploadedById(@Param("userId") Long userId);

    /**
     * Find materials requiring payment
     */
    @Query("SELECT m FROM Material m WHERE m.subjectGroup.id = :groupId AND m.requiresPayment = true AND m.isActive = true ORDER BY m.uploadedAt DESC")
    List<MaterialJpaEntity> findRequiringPaymentBySubjectGroupId(@Param("groupId") Long groupId);

    /**
     * Count active materials for a subject group
     */
    @Query("SELECT COUNT(m) FROM Material m WHERE m.subjectGroup.id = :groupId AND m.isActive = true")
    long countActiveBySubjectGroupId(@Param("groupId") Long groupId);

    /**
     * Count materials by type for a subject group
     */
    @Query("SELECT COUNT(m) FROM Material m WHERE m.subjectGroup.id = :groupId AND m.type = :type AND m.isActive = true")
    long countBySubjectGroupIdAndType(
            @Param("groupId") Long groupId,
            @Param("type") MaterialType type
    );

    /**
     * Find all distinct topics for a subject group
     */
    @Query("SELECT DISTINCT m.topic FROM Material m WHERE m.subjectGroup.id = :groupId AND m.topic IS NOT NULL AND m.isActive = true ORDER BY m.topic")
    List<String> findDistinctTopicsBySubjectGroupId(@Param("groupId") Long groupId);

    /**
     * Check if a file path already exists
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Material m WHERE m.filePath = :filePath")
    boolean existsByFilePath(@Param("filePath") String filePath);

    /**
     * Find materials by file name pattern
     */
    @Query("SELECT m FROM Material m WHERE m.subjectGroup.id = :groupId AND LOWER(m.fileName) LIKE LOWER(CONCAT('%', :pattern, '%')) AND m.isActive = true ORDER BY m.uploadedAt DESC")
    List<MaterialJpaEntity> findBySubjectGroupIdAndFileNameContaining(
            @Param("groupId") Long groupId,
            @Param("pattern") String pattern
    );

    /**
     * Calculate total storage size for a subject group
     */
    @Query("SELECT COALESCE(SUM(m.fileSize), 0) FROM Material m WHERE m.subjectGroup.id = :groupId AND m.isActive = true")
    Long calculateTotalSizeBySubjectGroupId(@Param("groupId") Long groupId);
}
