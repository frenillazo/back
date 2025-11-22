package acainfo.back.user.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.domain.model.UserStatus;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for UserJpaEntity.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<UserJpaEntity> findByStatus(UserStatus status);

    @Query("SELECT DISTINCT u FROM UserJpaEntity u JOIN u.roles r WHERE r.type = :roleType")
    List<UserJpaEntity> findByRoleType(@Param("roleType") RoleType roleType);

    @Query("SELECT DISTINCT u FROM UserJpaEntity u JOIN u.roles r WHERE r.type = :roleType AND u.status = :status")
    List<UserJpaEntity> findByRoleTypeAndStatus(@Param("roleType") RoleType roleType, @Param("status") UserStatus status);

    @Query("SELECT u FROM UserJpaEntity u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UserJpaEntity> searchByName(@Param("searchTerm") String searchTerm);
}
