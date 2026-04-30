package com.acainfo.intensive.infrastructure.adapter.out.persistence.repository;

import com.acainfo.intensive.domain.model.IntensiveStatus;
import com.acainfo.intensive.infrastructure.adapter.out.persistence.entity.IntensiveJpaEntity;
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
 * Spring Data JPA repository for IntensiveJpaEntity.
 */
@Repository
public interface JpaIntensiveRepository extends
        JpaRepository<IntensiveJpaEntity, Long>,
        JpaSpecificationExecutor<IntensiveJpaEntity> {

    long countByTeacherIdAndStatusIn(Long teacherId, List<IntensiveStatus> statuses);

    long countBySubjectIdAndStatusIn(Long subjectId, List<IntensiveStatus> statuses);

    long countBySubjectId(Long subjectId);

    List<IntensiveJpaEntity> findByStatus(IntensiveStatus status);

    @Query("SELECT i FROM IntensiveJpaEntity i WHERE i.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IntensiveJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
