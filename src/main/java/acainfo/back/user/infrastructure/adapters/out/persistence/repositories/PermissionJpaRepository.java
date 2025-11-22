package acainfo.back.user.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.user.infrastructure.adapters.out.persistence.entities.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for PermissionJpaEntity.
 */
@Repository
public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, Long> {

    Optional<PermissionJpaEntity> findByName(String name);

    boolean existsByName(String name);
}
