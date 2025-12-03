package com.acainfo.user.application.port.out;

import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Role persistence.
 * Defines the contract for Role repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface RoleRepositoryPort {

    /**
     * Save or update a role.
     *
     * @param role Domain role to persist
     * @return Persisted role with ID
     */
    Role save(Role role);

    /**
     * Find role by ID.
     *
     * @param id Role ID
     * @return Optional containing the role if found
     */
    Optional<Role> findById(Long id);

    /**
     * Find role by type.
     *
     * @param type Role type (ADMIN, TEACHER, STUDENT)
     * @return Optional containing the role if found
     */
    Optional<Role> findByType(RoleType type);

    /**
     * Find all roles.
     *
     * @return List of all roles
     */
    List<Role> findAll();

    /**
     * Check if role with type exists.
     *
     * @param type Role type
     * @return true if exists, false otherwise
     */
    boolean existsByType(RoleType type);
}
