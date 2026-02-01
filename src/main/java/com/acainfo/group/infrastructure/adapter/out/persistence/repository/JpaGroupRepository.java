package com.acainfo.group.infrastructure.adapter.out.persistence.repository;

import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.infrastructure.adapter.out.persistence.entity.SubjectGroupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for SubjectGroupJpaEntity.
 * Extends JpaSpecificationExecutor for Criteria Builder support.
 */
@Repository
public interface JpaGroupRepository extends
        JpaRepository<SubjectGroupJpaEntity, Long>,
        JpaSpecificationExecutor<SubjectGroupJpaEntity> {

    /**
     * Count groups by teacher ID and status in the given list.
     * Used to check if a teacher has active groups before deletion.
     */
    long countByTeacherIdAndStatusIn(Long teacherId, List<GroupStatus> statuses);

    /**
     * Count groups by subject ID and status in the given list.
     * Used to check if a subject has active groups before archiving.
     */
    long countBySubjectIdAndStatusIn(Long subjectId, List<GroupStatus> statuses);

    /**
     * Count all groups by subject ID (regardless of status).
     * Used for generating sequential group names.
     */
    long countBySubjectId(Long subjectId);
}
