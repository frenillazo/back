package acainfo.back.user.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.user.domain.model.AuditAction;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.AuditLogJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for AuditLogJpaEntity.
 */
@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, Long> {

    Page<AuditLogJpaEntity> findByUser(UserJpaEntity user, Pageable pageable);

    List<AuditLogJpaEntity> findByUserAndActionAndTimestampAfter(
            UserJpaEntity user,
            AuditAction action,
            LocalDateTime since
    );

    Page<AuditLogJpaEntity> findAllByOrderByTimestampDesc(Pageable pageable);

    List<AuditLogJpaEntity> findByAction(AuditAction action);

    List<AuditLogJpaEntity> findByEntityTypeAndEntityId(String entityType, Long entityId);

    @Modifying
    @Query("DELETE FROM AuditLogJpaEntity a WHERE a.timestamp < :olderThan")
    void deleteByTimestampBefore(@Param("olderThan") LocalDateTime olderThan);
}
