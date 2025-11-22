package acainfo.back.user.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;

/**
 * Permission domain model.
 * Pure POJO - NO infrastructure dependencies, NO JPA annotations.
 * Represents a permission that can be assigned to roles.
 */
@Value
@Builder(toBuilder = true)
public class PermissionDomain {

    Long id;

    @With
    String name;

    @With
    String description;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /**
     * Business rule: Permission name is required and must be unique.
     */
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Permission name is required");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Permission name must not exceed 100 characters");
        }
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("Description must not exceed 255 characters");
        }
    }

    /**
     * Checks if this permission matches a given name.
     */
    public boolean hasName(String permissionName) {
        return this.name.equalsIgnoreCase(permissionName);
    }
}
