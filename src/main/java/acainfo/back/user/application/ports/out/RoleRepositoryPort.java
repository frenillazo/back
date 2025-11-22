package acainfo.back.user.application.ports.out;

import acainfo.back.user.domain.model.RoleDomain;
import acainfo.back.user.domain.model.RoleType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Port (interface) for Role repository operations.
 * Defines the contract for role persistence.
 * Works with RoleDomain (pure domain model).
 */
public interface RoleRepositoryPort {

    /**
     * Saves a role (create or update).
     *
     * @param role the role to save
     * @return the saved role
     */
    RoleDomain save(RoleDomain role);

    /**
     * Finds a role by ID.
     *
     * @param id the role ID
     * @return Optional containing the role if found
     */
    Optional<RoleDomain> findById(Long id);

    /**
     * Finds a role by type.
     *
     * @param type the role type
     * @return Optional containing the role if found
     */
    Optional<RoleDomain> findByType(RoleType type);

    /**
     * Checks if a role exists by type.
     *
     * @param type the role type
     * @return true if role exists, false otherwise
     */
    boolean existsByType(RoleType type);

    /**
     * Finds a role by ID with its permissions eagerly loaded.
     *
     * @param id the role ID
     * @return Optional containing the role with permissions if found
     */
    Optional<RoleDomain> findByIdWithPermissions(Long id);

    /**
     * Finds a role by type with permissions eagerly loaded.
     *
     * @param type the role type
     * @return Optional containing the role with permissions if found
     */
    Optional<RoleDomain> findByTypeWithPermissions(RoleType type);

    /**
     * Finds all roles.
     *
     * @return list of all roles
     */
    List<RoleDomain> findAll();

    /**
     * Finds all roles with permissions eagerly loaded.
     *
     * @return set of all roles with permissions
     */
    Set<RoleDomain> findAllWithPermissions();

    /**
     * Deletes a role by ID.
     *
     * @param id the role ID
     */
    void deleteById(Long id);

    /**
     * Checks if a role exists by ID.
     *
     * @param id the role ID
     * @return true if role exists, false otherwise
     */
    boolean existsById(Long id);
}
