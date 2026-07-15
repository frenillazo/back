package com.acainfo.material.infrastructure.adapter.out.persistence.repository;

import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialFolderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for MaterialFolder entities.
 */
@Repository
public interface JpaMaterialFolderRepository extends JpaRepository<MaterialFolderJpaEntity, Long> {

    /**
     * Find all folders for a subject ordered by manual position.
     */
    List<MaterialFolderJpaEntity> findBySubjectIdOrderByPositionAscNameAsc(Long subjectId);

    /**
     * Check name uniqueness within a subject.
     */
    boolean existsBySubjectIdAndName(Long subjectId, String name);
}
