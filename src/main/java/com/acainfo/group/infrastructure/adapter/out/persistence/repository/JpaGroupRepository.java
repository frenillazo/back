package com.acainfo.group.infrastructure.adapter.out.persistence.repository;

import com.acainfo.group.infrastructure.adapter.out.persistence.entity.SubjectGroupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for SubjectGroupJpaEntity.
 * Extends JpaSpecificationExecutor for Criteria Builder support.
 */
@Repository
public interface JpaGroupRepository extends
        JpaRepository<SubjectGroupJpaEntity, Long>,
        JpaSpecificationExecutor<SubjectGroupJpaEntity> {
}
