package com.acainfo.user.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Role domain entity - POJO with Lombok to reduce boilerplate.
 * Represents a role that can be assigned to users.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "type")
@ToString
public class Role {

    private Long id;
    private RoleType type;
    private String description;

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
}
