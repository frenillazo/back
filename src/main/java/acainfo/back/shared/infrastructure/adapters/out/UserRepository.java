package acainfo.back.shared.infrastructure.adapters.out;

import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.domain.model.UserStatus;
import acainfo.back.shared.domain.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (case-insensitive)
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check if user exists by email (case-insensitive)
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Find all users by status
     */
    List<User> findByStatus(UserStatus status);

    User findByEmail(String email);

    /**
     * Find users by role type
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.type = :roleType")
    List<User> findByRoleType(@Param("roleType") RoleType roleType);

    /**
     * Find active users with specific role
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE u.status = 'ACTIVE' AND r.type = :roleType")
    List<User> findActiveUsersByRoleType(@Param("roleType") RoleType roleType);

    /**
     * Find users by first name or last name containing (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Count users by status
     */
    long countByStatus(UserStatus status);

    /**
     * Find all active users
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findAllActive();
}
