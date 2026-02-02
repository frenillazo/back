package com.acainfo.user.application.port.out;

import com.acainfo.user.application.dto.UserFilters;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Output port for User persistence.
 * Defines the contract for User repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface UserRepositoryPort {

    /**
     * Save or update a user.
     *
     * @param user Domain user to persist
     * @return Persisted user with ID
     */
    User save(User user);

    /**
     * Find user by ID.
     *
     * @param id User ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(Long id);

    /**
     * Find user by email (case insensitive).
     *
     * @param email Email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists (case insensitive).
     *
     * @param email Email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find users with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of users matching filters
     */
    Page<User> findWithFilters(UserFilters filters);

    /**
     * Delete a user.
     *
     * @param user User to delete
     */
    void delete(User user);

    /**
     * Delete user by ID.
     *
     * @param id User ID
     */
    void deleteById(Long id);

    /**
     * Find user IDs whose email contains the given search term (case insensitive).
     *
     * @param emailSearch partial email to search for
     * @return list of user IDs matching the search
     */
    List<Long> findIdsByEmailContaining(String emailSearch);

    /**
     * Find users with a specific status created before a given date.
     * Used for cleanup tasks like removing unverified users.
     *
     * @param status User status to filter
     * @param createdBefore Users created before this date
     * @return List of users matching the criteria
     */
    List<User> findByStatusAndCreatedAtBefore(UserStatus status, LocalDateTime createdBefore);
}
