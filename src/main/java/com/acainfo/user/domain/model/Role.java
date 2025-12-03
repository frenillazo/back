package com.acainfo.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Role domain entity - Anemic model with Lombok.
 * Business logic resides in application services.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "type")
@ToString
public class Role {

    private Long id;
    private RoleType type;
    private String description;

    /**
     * Query method: Check if this is an admin role.
     */
    public boolean isAdmin() {
        return type == RoleType.ADMIN;
    }

    /**
     * Query method: Check if this is a teacher role.
     */
    public boolean isTeacher() {
        return type == RoleType.TEACHER;
    }

    /**
     * Query method: Check if this is a student role.
     */
    public boolean isStudent() {
        return type == RoleType.STUDENT;
    }
}
