package com.acainfo.material.infrastructure.adapter.out.persistence.repository;

import com.acainfo.material.domain.model.MaterialAiJobStatus;
import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialAiJobJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Spring Data JPA repository for MaterialAiJob entities.
 */
@Repository
public interface JpaMaterialAiJobRepository extends JpaRepository<MaterialAiJobJpaEntity, Long> {

    /**
     * Bulk-fail jobs left in the given statuses (orphans after a server restart).
     * Bulk update skips auditing, so updated_at is set explicitly.
     */
    @Modifying
    @Query("""
            UPDATE MaterialAiJobJpaEntity j
            SET j.status = com.acainfo.material.domain.model.MaterialAiJobStatus.FAILED,
                j.errorMessage = :errorMessage,
                j.updatedAt = :now
            WHERE j.status IN :statuses
            """)
    int failAllWithStatuses(@Param("statuses") Collection<MaterialAiJobStatus> statuses,
                            @Param("errorMessage") String errorMessage,
                            @Param("now") LocalDateTime now);
}
