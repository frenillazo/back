package com.acainfo.user.infrastructure.adapter.out.persistence.repository;

import com.acainfo.user.domain.model.UserStatus;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * Find user IDs whose email contains the given search term (case insensitive).
     */
    @Query("SELECT u.id FROM UserJpaEntity u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :emailSearch, '%'))")
    List<Long> findIdsByEmailContainingIgnoreCase(@Param("emailSearch") String emailSearch);

    /**
     * Find users with a specific status created before a given date.
     * Used for cleanup tasks like removing unverified users.
     */
    List<UserJpaEntity> findByStatusAndCreatedAtBefore(UserStatus status, LocalDateTime createdBefore);
}
