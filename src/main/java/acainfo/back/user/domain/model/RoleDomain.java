package acainfo.back.user.domain.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Role domain model.
 * Pure POJO - NO infrastructure dependencies, NO JPA annotations.
 * Represents a role that groups permissions and is assigned to users.
 */
@Value
@Builder(toBuilder = true)
public class RoleDomain {

    Long id;

    @With
    RoleType type;

    @With
    String name;

    @With
    String description;

    @Singular
    Set<PermissionDomain> permissions;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Business rule: Role must have a type and name.
     */
    public void validate() {
        if (type == null) {
            throw new IllegalArgumentException("Role type is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Role name must not exceed 100 characters");
        }
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("Description must not exceed 255 characters");
        }
    }

    /**
     * Checks if this role has a specific permission.
     */
    public boolean hasPermission(String permissionName) {
        if (permissions == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(p -> p.hasName(permissionName));
    }

    /**
     * Checks if this role has a specific permission.
     */
    public boolean hasPermission(PermissionDomain permission) {
        if (permissions == null || permission == null) {
            return false;
        }
        return permissions.contains(permission);
    }

    /**
     * Gets the number of permissions assigned to this role.
     */
    public int getPermissionCount() {
        return permissions == null ? 0 : permissions.size();
    }

    /**
     * Checks if this is an admin role.
     */
    public boolean isAdmin() {
        return type == RoleType.ADMIN;
    }

    /**
     * Checks if this is a teacher role.
     */
    public boolean isTeacher() {
        return type == RoleType.TEACHER;
    }

    /**
     * Checks if this is a student role.
     */
    public boolean isStudent() {
        return type == RoleType.STUDENT;
    }
}
