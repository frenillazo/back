package com.acainfo.group.infrastructure.adapter.out.persistence.repository;

import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.infrastructure.adapter.out.persistence.entity.SubjectGroupJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    /**
     * Find a group by ID with a pessimistic write lock.
     * Used to prevent concurrent modifications during enrollment approval
     * and waiting list promotion (capacity check-then-act).
     */
    @Query("SELECT g FROM SubjectGroupJpaEntity g WHERE g.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SubjectGroupJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
