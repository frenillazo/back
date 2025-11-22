package acainfo.back.user.application.ports.out;

import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.domain.model.UserDomain;
import acainfo.back.user.domain.model.UserStatus;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface) for User repository operations.
 * Defines the contract for user persistence.
 * Works with UserDomain (pure domain model).
 *
 * This is part of the hexagonal architecture - the application layer
 * defines what it needs, and the infrastructure layer implements it.
 */
public interface UserRepositoryPort {

    /**
     * Saves a user (create or update).
     *
     * @param user the user to save
     * @return the saved user
     */
    UserDomain save(UserDomain user);

    /**
     * Finds a user by ID.
     *
     * @param id the user ID
     * @return Optional containing the user if found
     */
    Optional<UserDomain> findById(Long id);

    /**
     * Finds a user by email (case-insensitive).
     *
     * @param email the user email
     * @return Optional containing the user if found
     */
    Optional<UserDomain> findByEmailIgnoreCase(String email);

    /**
     * Finds a user by email.
     *
     * @param email the user email
     * @return the user if found, null otherwise
     */
    UserDomain findByEmail(String email);

    /**
     * Checks if a user exists by email (case-insensitive).
     *
     * @param email the user email
     * @return true if user exists, false otherwise
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Finds all users by status.
     *
     * @param status the user status
     * @return list of users
     */
    List<UserDomain> findByStatus(UserStatus status);

    /**
     * Finds users by role type.
     *
     * @param roleType the role type
     * @return list of users
     */
    List<UserDomain> findByRoleType(RoleType roleType);

    /**
     * Finds active users with specific role.
     *
     * @param roleType the role type
     * @return list of active users
     */
    List<UserDomain> findActiveUsersByRoleType(RoleType roleType);

    /**
     * Searches users by name (first name or last name containing the search term).
     *
     * @param searchTerm the search term
     * @return list of users
     */
    List<UserDomain> searchByName(String searchTerm);

    /**
     * Counts users by status.
     *
     * @param status the user status
     * @return count of users
     */
    long countByStatus(UserStatus status);

    /**
     * Finds all active users.
     *
     * @return list of active users
     */
    List<UserDomain> findAllActive();

    /**
     * Finds all users.
     *
     * @return list of all users
     */
    List<UserDomain> findAll();

    /**
     * Deletes a user by ID.
     *
     * @param id the user ID
     */
    void deleteById(Long id);

    /**
     * Checks if a user exists by ID.
     *
     * @param id the user ID
     * @return true if user exists, false otherwise
     */
    boolean existsById(Long id);
}
