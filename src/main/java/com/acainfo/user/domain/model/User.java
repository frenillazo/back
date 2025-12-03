package com.acainfo.user.domain.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * User domain entity - Pure POJO without framework annotations.
 * Represents a user in the system with their roles and authentication information.
 */
public class User {

    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private Set<Role> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Private constructor to enforce builder pattern
    private User(Long id, String email, String password, String firstName, String lastName,
                 UserStatus status, Set<Role> roles, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Creates a new User for registration.
     */
    public static User create(String email, String password, String firstName, String lastName) {
        validateEmail(email);
        validatePassword(password);
        validateName(firstName, "First name");
        validateName(lastName, "Last name");

        return new User(
                null,
                email.toLowerCase().trim(),
                password,
                firstName.trim(),
                lastName.trim(),
                UserStatus.PENDING_ACTIVATION,
                new HashSet<>(),
                null,
                null
        );
    }

    /**
     * Reconstructs a User from persistence (with ID and timestamps).
     */
    public static User reconstitute(Long id, String email, String password, String firstName, String lastName,
                                     UserStatus status, Set<Role> roles, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new User(id, email, password, firstName, lastName, status, roles, createdAt, updatedAt);
    }

    // Validation methods
    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }

    private static void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    // Business logic methods - Role checking (simplified security without Permission entity)
    public boolean isAdmin() {
        return hasRole(RoleType.ADMIN);
    }

    public boolean isTeacher() {
        return hasRole(RoleType.TEACHER);
    }

    public boolean isStudent() {
        return hasRole(RoleType.STUDENT);
    }

    private boolean hasRole(RoleType roleType) {
        return roles.stream()
                .anyMatch(role -> role.getType() == roleType);
    }

    // Business rules for authorization
    public boolean canManageGroups() {
        return isAdmin();
    }

    public boolean canRegisterAttendance() {
        return isAdmin() || isTeacher();
    }

    public boolean canUploadMaterials() {
        return isAdmin() || isTeacher();
    }

    public boolean canManageUsers() {
        return isAdmin();
    }

    // Status management
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }

    // Role management
    public void addRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    // Update methods
    public void updateProfile(String firstName, String lastName) {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
    }

    public void updatePassword(String newPassword) {
        validatePassword(newPassword);
        this.password = newPassword;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", status=" + status +
                ", roles=" + roles.size() +
                '}';
    }
}
