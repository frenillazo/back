package com.acainfo.enrollment.infrastructure.adapter.out.persistence.repository;

import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity.EnrollmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for EnrollmentJpaEntity.
 * Extends JpaSpecificationExecutor for Criteria Builder support.
 */
@Repository
public interface JpaEnrollmentRepository extends
        JpaRepository<EnrollmentJpaEntity, Long>,
        JpaSpecificationExecutor<EnrollmentJpaEntity> {

    /**
     * Find all enrollments for a student.
     */
    List<EnrollmentJpaEntity> findByStudentId(Long studentId);

    /**
     * Find all enrollments for a group.
     */
    List<EnrollmentJpaEntity> findByGroupId(Long groupId);

    /**
     * Find enrollments by student and status.
     */
    List<EnrollmentJpaEntity> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    /**
     * Find enrollments by group and status.
     */
    List<EnrollmentJpaEntity> findByGroupIdAndStatus(Long groupId, EnrollmentStatus status);

    /**
     * Find a specific enrollment by student and group.
     */
    Optional<EnrollmentJpaEntity> findByStudentIdAndGroupId(Long studentId, Long groupId);

    /**
     * Check if student is already enrolled (active or waiting) in a group.
     */
    boolean existsByStudentIdAndGroupIdAndStatusIn(Long studentId, Long groupId, List<EnrollmentStatus> statuses);

    /**
     * Count active enrollments for a group.
     */
    long countByGroupIdAndStatus(Long groupId, EnrollmentStatus status);

    /**
     * Find waiting list for a group, ordered by position (FIFO).
     */
    List<EnrollmentJpaEntity> findByGroupIdAndStatusOrderByWaitingListPositionAsc(
            Long groupId, EnrollmentStatus status);

    /**
     * Find the next waiting list position for a group.
     */
    @Query("SELECT COALESCE(MAX(e.waitingListPosition), 0) + 1 FROM EnrollmentJpaEntity e " +
           "WHERE e.groupId = :groupId AND e.status = 'WAITING_LIST'")
    int findNextWaitingListPosition(@Param("groupId") Long groupId);

    /**
     * Decrement waiting list positions after a student leaves.
     * Called when someone leaves the waiting list to maintain FIFO order.
     */
    @Modifying
    @Query("UPDATE EnrollmentJpaEntity e SET e.waitingListPosition = e.waitingListPosition - 1 " +
           "WHERE e.groupId = :groupId AND e.status = 'WAITING_LIST' " +
           "AND e.waitingListPosition > :position")
    void decrementWaitingListPositionsAfter(@Param("groupId") Long groupId, @Param("position") int position);
}
