package acainfo.back.user.domain.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User domain model.
 * Pure POJO - NO infrastructure dependencies, NO JPA annotations.
 * Represents a user in the system with authentication and authorization.
 */
@Value
@Builder(toBuilder = true)
public class UserDomain {

    Long id;

    @With
    String email;

    @With
    String password;

    @With
    String firstName;

    @With
    String lastName;

    @With
    String phone;

    @With
    @Builder.Default
    UserStatus status = UserStatus.ACTIVE;

    @Singular
    Set<RoleDomain> roles;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Business rules validation.
     */
    public void validate() {
        validateEmail();
        validatePassword();
        validateFirstName();
        validateLastName();
        validatePhone();
        validateStatus();
    }

    private void validateEmail() {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email must be valid");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("Email must not exceed 255 characters");
        }
    }

    private void validatePassword() {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }

    private void validateFirstName() {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (firstName.length() > 100) {
            throw new IllegalArgumentException("First name must not exceed 100 characters");
        }
    }

    private void validateLastName() {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (lastName.length() > 100) {
            throw new IllegalArgumentException("Last name must not exceed 100 characters");
        }
    }

    private void validatePhone() {
        if (phone != null && phone.length() > 20) {
            throw new IllegalArgumentException("Phone must not exceed 20 characters");
        }
    }

    private void validateStatus() {
        if (status == null) {
            throw new IllegalArgumentException("User status is required");
        }
    }

    /**
     * Returns the full name of the user.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the last login time (using updatedAt as proxy).
     */
    public LocalDateTime getLastLogin() {
        return updatedAt;
    }

    /**
     * Checks if the user has a specific role type.
     */
    public boolean hasRole(RoleType roleType) {
        if (roles == null || roleType == null) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> role.getType() == roleType);
    }

    /**
     * Checks if the user has a specific permission.
     */
    public boolean hasPermission(String permissionName) {
        if (roles == null || permissionName == null) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> role.hasPermission(permissionName));
    }

    /**
     * Checks if the user is active.
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Checks if the user is suspended.
     */
    public boolean isSuspended() {
        return status == UserStatus.SUSPENDED;
    }

    /**
     * Checks if the user is inactive.
     */
    public boolean isInactive() {
        return status == UserStatus.INACTIVE;
    }

    /**
     * Checks if the user is a teacher.
     */
    public boolean isTeacher() {
        return hasRole(RoleType.TEACHER);
    }

    /**
     * Checks if the user is an admin.
     */
    public boolean isAdmin() {
        return hasRole(RoleType.ADMIN);
    }

    /**
     * Checks if the user is a student.
     */
    public boolean isStudent() {
        return hasRole(RoleType.STUDENT);
    }

    /**
     * Gets the number of roles assigned to this user.
     */
    public int getRoleCount() {
        return roles == null ? 0 : roles.size();
    }

    /**
     * Checks if the user can perform an action based on permission.
     */
    public boolean canPerform(String permission) {
        return isActive() && hasPermission(permission);
    }
}
