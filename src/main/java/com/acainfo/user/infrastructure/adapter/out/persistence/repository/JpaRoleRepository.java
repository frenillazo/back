package com.acainfo.user.infrastructure.adapter.out.persistence.repository;

import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for RoleJpaEntity.
 */
@Repository
public interface JpaRoleRepository extends JpaRepository<RoleJpaEntity, Long> {

    /**
     * Find role by type.
     */
    Optional<RoleJpaEntity> findByType(RoleType type);

    /**
     * Check if role with type exists.
     */
    boolean existsByType(RoleType type);
}
