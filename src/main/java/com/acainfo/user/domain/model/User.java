package com.acainfo.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User domain entity - Anemic model with Lombok.
 * Business logic (including validations) resides in application services.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "email")
@ToString(exclude = "password")
public class User {

    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserStatus status;

    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========== Query Methods Only (No Business Logic) ==========

    /**
     * Check if user has admin role.
     */
    public boolean isAdmin() {
        return roles.stream().anyMatch(Role::isAdmin);
    }

    /**
     * Check if user has teacher role.
     */
    public boolean isTeacher() {
        return roles.stream().anyMatch(Role::isTeacher);
    }

    /**
     * Check if user has student role.
     */
    public boolean isStudent() {
        return roles.stream().anyMatch(Role::isStudent);
    }

    /**
     * Check if user account status is active.
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Check if user account status is blocked.
     */
    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }

    /**
     * Get full name (computed property).
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
