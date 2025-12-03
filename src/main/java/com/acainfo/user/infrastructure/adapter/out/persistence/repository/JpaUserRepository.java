package com.acainfo.user.infrastructure.adapter.out.persistence.repository;

import com.acainfo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for UserJpaEntity.
 * Extends JpaSpecificationExecutor for Criteria Builder support.
 */
@Repository
public interface JpaUserRepository extends
        JpaRepository<UserJpaEntity, Long>,
        JpaSpecificationExecutor<UserJpaEntity> {

    /**
     * Find user by email (case insensitive).
     */
    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);

    /**
     * Check if email exists (case insensitive).
     */
    boolean existsByEmailIgnoreCase(String email);
}
