package acainfo.back.user.application.ports.out;

import acainfo.back.user.domain.model.PermissionDomain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Port (interface) for Permission repository operations.
 * Defines the contract for permission persistence.
 * Works with PermissionDomain (pure domain model).
 */
public interface PermissionRepositoryPort {

    /**
     * Saves a permission (create or update).
     *
     * @param permission the permission to save
     * @return the saved permission
     */
    PermissionDomain save(PermissionDomain permission);

    /**
     * Finds a permission by ID.
     *
     * @param id the permission ID
     * @return Optional containing the permission if found
     */
    Optional<PermissionDomain> findById(Long id);

    /**
     * Finds a permission by name.
     *
     * @param name the permission name
     * @return Optional containing the permission if found
     */
    Optional<PermissionDomain> findByName(String name);

    /**
     * Finds permissions by names.
     *
     * @param names the permission names
     * @return set of permissions
     */
    Set<PermissionDomain> findByNameIn(List<String> names);

    /**
     * Checks if a permission exists by name.
     *
     * @param name the permission name
     * @return true if permission exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Finds all permissions.
     *
     * @return list of all permissions
     */
    List<PermissionDomain> findAll();

    /**
     * Deletes a permission by ID.
     *
     * @param id the permission ID
     */
    void deleteById(Long id);

    /**
     * Checks if a permission exists by ID.
     *
     * @param id the permission ID
     * @return true if permission exists, false otherwise
     */
    boolean existsById(Long id);
}
