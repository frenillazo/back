package com.acainfo.material.infrastructure.adapter.out.persistence.repository;

import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
