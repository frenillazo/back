package acainfo.back.user.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for RoleJpaEntity.
 */
@Repository
public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {

    Optional<RoleJpaEntity> findByType(RoleType type);

    Optional<RoleJpaEntity> findByName(String name);

    boolean existsByType(RoleType type);

    boolean existsByName(String name);
}
