package com.acainfo.enrollment.infrastructure.adapter.out.persistence.repository;

import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity.GroupRequestJpaEntity;
import com.acainfo.group.domain.model.GroupType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for GroupRequestJpaEntity.
 * Extends JpaSpecificationExecutor for Criteria Builder support.
 */
@Repository
public interface JpaGroupRequestRepository extends
        JpaRepository<GroupRequestJpaEntity, Long>,
        JpaSpecificationExecutor<GroupRequestJpaEntity> {

    /**
     * Find all group requests for a subject.
     */
    List<GroupRequestJpaEntity> findBySubjectId(Long subjectId);

    /**
     * Find all group requests by requester.
     */
    List<GroupRequestJpaEntity> findByRequesterId(Long requesterId);

    /**
     * Find group requests by status.
     */
    List<GroupRequestJpaEntity> findByStatus(GroupRequestStatus status);

    /**
     * Find pending group requests for a subject with a specific type.
     */
    List<GroupRequestJpaEntity> findBySubjectIdAndRequestedGroupTypeAndStatus(
            Long subjectId, GroupType requestedGroupType, GroupRequestStatus status);

    /**
     * Find expired pending requests (for scheduled cleanup job).
     */
    List<GroupRequestJpaEntity> findByStatusAndExpiresAtBefore(
            GroupRequestStatus status, LocalDateTime dateTime);
}
