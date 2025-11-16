package acainfo.back.infrastructure.adapters.out;

import acainfo.back.domain.model.Role;
import acainfo.back.domain.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by type
     */
    Optional<Role> findByType(RoleType type);

    /**
     * Check if role exists by type
     */
    boolean existsByType(RoleType type);

    /**
     * Find role with its permissions eagerly loaded
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    /**
     * Find role by type with permissions eagerly loaded
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.type = :type")
    Optional<Role> findByTypeWithPermissions(@Param("type") RoleType type);

    /**
     * Find all roles with permissions
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions")
    Set<Role> findAllWithPermissions();
}
