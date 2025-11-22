package acainfo.back.enrollment.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.enrollment.domain.model.GroupRequestStatus;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities.GroupRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for GroupRequestJpaEntity.
 * Provides CRUD operations and custom queries for group request management.
 */
@Repository
public interface GroupRequestJpaRepository extends JpaRepository<GroupRequestJpaEntity, Long> {

    /**
     * Find all group requests by subject ID
     */
    @Query("SELECT gr FROM GroupRequestJpaEntity gr WHERE gr.subject.id = :subjectId")
    List<GroupRequestJpaEntity> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find all group requests by requester ID
     */
    @Query("SELECT gr FROM GroupRequestJpaEntity gr WHERE gr.requestedBy.id = :requesterId")
    List<GroupRequestJpaEntity> findByRequesterId(@Param("requesterId") Long requesterId);

    /**
     * Find all group requests by status
     */
    List<GroupRequestJpaEntity> findByStatus(GroupRequestStatus status);

    /**
     * Find all pending group requests
     */
    default List<GroupRequestJpaEntity> findAllPending() {
        return findByStatus(GroupRequestStatus.PENDIENTE);
    }

    /**
     * Find pending group requests for a subject
     */
    @Query("SELECT gr FROM GroupRequestJpaEntity gr WHERE gr.subject.id = :subjectId AND gr.status = 'PENDIENTE'")
    List<GroupRequestJpaEntity> findPendingBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find a single pending request for a subject
     * Should return at most one result (business rule: only one pending request per subject)
     */
    @Query("SELECT gr FROM GroupRequestJpaEntity gr WHERE gr.subject.id = :subjectId AND gr.status = 'PENDIENTE'")
    Optional<GroupRequestJpaEntity> findPendingRequestBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Check if a pending request exists for a subject
     */
    @Query("SELECT CASE WHEN COUNT(gr) > 0 THEN true ELSE false END FROM GroupRequestJpaEntity gr " +
           "WHERE gr.subject.id = :subjectId AND gr.status = 'PENDIENTE'")
    boolean existsPendingRequestBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find group requests where a student is a supporter
     */
    @Query("SELECT gr FROM GroupRequestJpaEntity gr JOIN gr.supporters s WHERE s.id = :studentId")
    List<GroupRequestJpaEntity> findRequestsSupportedByStudent(@Param("studentId") Long studentId);

    /**
     * Check if a student is a supporter of a specific request
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM GroupRequestJpaEntity gr JOIN gr.supporters s " +
           "WHERE gr.id = :requestId AND s.id = :studentId")
    boolean isStudentSupporter(@Param("requestId") Long requestId, @Param("studentId") Long studentId);

    /**
     * Find group requests with minimum supporters reached
     */
    @Query("SELECT gr FROM GroupRequestJpaEntity gr WHERE SIZE(gr.supporters) >= :minSupporters AND gr.status = 'PENDIENTE'")
    List<GroupRequestJpaEntity> findPendingRequestsWithMinimumSupporters(@Param("minSupporters") int minSupporters);

    /**
     * Count pending requests for a subject
     */
    @Query("SELECT COUNT(gr) FROM GroupRequestJpaEntity gr WHERE gr.subject.id = :subjectId AND gr.status = 'PENDIENTE'")
    long countPendingBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Count pending requests by student ID
     */
    @Query("SELECT COUNT(gr) FROM GroupRequestJpaEntity gr WHERE gr.requestedBy.id = :studentId AND gr.status = 'PENDIENTE'")
    int countPendingByStudentId(@Param("studentId") Long studentId);
}
