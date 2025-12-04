package com.acainfo.subject.infrastructure.adapter.out.persistence.repository;

import com.acainfo.subject.infrastructure.adapter.out.persistence.entity.SubjectJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for SubjectJpaEntity.
 * Extends JpaSpecificationExecutor for Criteria Builder support.
 */
@Repository
public interface JpaSubjectRepository extends
        JpaRepository<SubjectJpaEntity, Long>,
        JpaSpecificationExecutor<SubjectJpaEntity> {

    /**
     * Find subject by code (case insensitive).
     *
     * @param code Subject code
     * @return Optional containing the subject if found
     */
    Optional<SubjectJpaEntity> findByCodeIgnoreCase(String code);

    /**
     * Check if subject code exists (case insensitive).
     *
     * @param code Subject code
     * @return true if exists, false otherwise
     */
    boolean existsByCodeIgnoreCase(String code);
}
