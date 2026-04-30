package com.acainfo.material.infrastructure.adapter.out.persistence.repository;

import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for Material entities.
 */
@Repository
public interface JpaMaterialRepository extends JpaRepository<MaterialJpaEntity, Long>,
        JpaSpecificationExecutor<MaterialJpaEntity> {

    /**
     * Find all materials for a subject ordered by upload date descending.
     */
    List<MaterialJpaEntity> findBySubjectIdOrderByUploadedAtDesc(Long subjectId);

    /**
     * Find all materials uploaded by a user.
     */
    List<MaterialJpaEntity> findByUploadedByIdOrderByUploadedAtDesc(Long uploadedById);

    /**
     * Find materials by subject and file extension.
     */
    List<MaterialJpaEntity> findBySubjectIdAndFileExtension(Long subjectId, String fileExtension);

    /**
     * Count materials for a subject.
     */
    long countBySubjectId(Long subjectId);

    /**
     * Find recent materials for given subjects uploaded after a certain date.
     */
    @Query("SELECT m FROM MaterialJpaEntity m WHERE m.subjectId IN :subjectIds AND m.uploadedAt >= :since ORDER BY m.uploadedAt DESC")
    List<MaterialJpaEntity> findRecentBySubjectIds(@Param("subjectIds") List<Long> subjectIds, @Param("since") LocalDateTime since);

    /**
     * Batch update downloadDisabled flag.
     * If disabled=false, also resets downloadEnabledAt; otherwise leaves it unchanged.
     */
    @Modifying
    @Query("UPDATE MaterialJpaEntity m " +
            "SET m.downloadDisabled = :disabled, " +
            "    m.downloadEnabledAt = CASE WHEN :disabled = false THEN :enabledAt ELSE m.downloadEnabledAt END " +
            "WHERE m.id IN :ids")
    int batchUpdateDownloadDisabled(@Param("ids") List<Long> ids,
                                    @Param("disabled") boolean disabled,
                                    @Param("enabledAt") LocalDateTime enabledAt);

    /**
     * Batch update visibility flag.
     * If visible=true, also resets visibilityEnabledAt; otherwise leaves it unchanged.
     */
    @Modifying
    @Query("UPDATE MaterialJpaEntity m " +
            "SET m.visible = :visible, " +
            "    m.visibilityEnabledAt = CASE WHEN :visible = true THEN :enabledAt ELSE m.visibilityEnabledAt END " +
            "WHERE m.id IN :ids")
    int batchUpdateVisibility(@Param("ids") List<Long> ids,
                              @Param("visible") boolean visible,
                              @Param("enabledAt") LocalDateTime enabledAt);

    /**
     * Find materials currently active (visible AND download enabled) where both flags
     * have been active since at least the given threshold timestamp.
     */
    @Query("SELECT m FROM MaterialJpaEntity m " +
            "WHERE m.visible = true AND m.downloadDisabled = false " +
            "  AND m.visibilityEnabledAt IS NOT NULL AND m.visibilityEnabledAt <= :threshold " +
            "  AND m.downloadEnabledAt IS NOT NULL AND m.downloadEnabledAt <= :threshold")
    List<MaterialJpaEntity> findExpiredActiveMaterials(@Param("threshold") LocalDateTime threshold);
}
