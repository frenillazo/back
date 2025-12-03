package com.acainfo.user.domain.model;

import java.util.Objects;

/**
 * Role domain entity - Pure POJO without framework annotations.
 * Represents a role that can be assigned to users.
 */
public class Role {

    private Long id;
    private RoleType type;
    private String description;

    // Private constructor to enforce builder pattern
    private Role(Long id, RoleType type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
    }

    /**
     * Creates a new Role with the given type.
     */
    public static Role create(RoleType type, String description) {
        validateType(type);
        return new Role(null, type, description);
    }

    /**
     * Reconstructs a Role from persistence (with ID).
     */
    public static Role reconstitute(Long id, RoleType type, String description) {
        validateType(type);
        return new Role(id, type, description);
    }

    private static void validateType(RoleType type) {
        if (type == null) {
            throw new IllegalArgumentException("Role type cannot be null");
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public RoleType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    // Business logic methods
    public boolean isAdmin() {
        return type == RoleType.ADMIN;
    }

    public boolean isTeacher() {
        return type == RoleType.TEACHER;
    }

    public boolean isStudent() {
        return type == RoleType.STUDENT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return type == role.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", type=" + type +
                ", description='" + description + '\'' +
                '}';
    }
}
