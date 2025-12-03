package com.acainfo.user.domain.model;

import com.acainfo.shared.domain.exception.ValidationException;
import lombok.AllArgsConstructor;
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
 * Business logic resides in application services.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "email")
@ToString(exclude = "password")
public class User {

    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private Set<Role> roles = new HashSet<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========== Query Methods (Simple Checks) ==========

    /**
     * Check if user is an admin.
     */
    public boolean isAdmin() {
        return roles.stream().anyMatch(Role::isAdmin);
    }

    /**
     * Check if user is a teacher.
     */
    public boolean isTeacher() {
        return roles.stream().anyMatch(Role::isTeacher);
    }

    /**
     * Check if user is a student.
     */
    public boolean isStudent() {
        return roles.stream().anyMatch(Role::isStudent);
    }

    /**
     * Check if user account is active.
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Check if user account is blocked.
     */
    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }

    /**
     * Get full name.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ========== Basic Invariant Validation (in Setters) ==========

    /**
     * Set email with basic format validation.
     */
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be null or empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Invalid email format");
        }
        this.email = email.toLowerCase().trim();
    }

    /**
     * Set first name with basic validation.
     */
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("First name cannot be null or empty");
        }
        this.firstName = firstName.trim();
    }

    /**
     * Set last name with basic validation.
     */
    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Last name cannot be null or empty");
        }
        this.lastName = lastName.trim();
    }
}
