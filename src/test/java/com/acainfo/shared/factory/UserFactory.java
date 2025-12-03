package com.acainfo.shared.factory;

import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.RoleJpaEntity;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory for creating User test data.
 * Provides fluent API for building User domain and JPA entities with sensible defaults.
 */
public class UserFactory {

    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private Set<Role> roles;
    private Set<RoleJpaEntity> rolesJpa;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserFactory() {
        // Default values
        this.id = 1L;
        this.email = "student@test.com";
        this.password = "$2a$10$dummyHashedPassword"; // BCrypt format
        this.firstName = "John";
        this.lastName = "Doe";
        this.status = UserStatus.ACTIVE;
        this.roles = new HashSet<>(Set.of(RoleFactory.defaultStudentRole()));
        this.rolesJpa = new HashSet<>(Set.of(RoleFactory.defaultStudentRoleJpa()));
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static UserFactory builder() {
        return new UserFactory();
    }

    public UserFactory id(Long id) {
        this.id = id;
        return this;
    }

    public UserFactory email(String email) {
        this.email = email;
        return this;
    }

    public UserFactory password(String password) {
        this.password = password;
        return this;
    }

    public UserFactory firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserFactory lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserFactory status(UserStatus status) {
        this.status = status;
        return this;
    }

    public UserFactory roles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }

    public UserFactory rolesJpa(Set<RoleJpaEntity> rolesJpa) {
        this.rolesJpa = rolesJpa;
        return this;
    }

    public UserFactory createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UserFactory updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    // ========== Convenience Methods ==========

    public UserFactory asStudent() {
        this.email = "student@test.com";
        this.roles = new HashSet<>(Set.of(RoleFactory.defaultStudentRole()));
        this.rolesJpa = new HashSet<>(Set.of(RoleFactory.defaultStudentRoleJpa()));
        return this;
    }

    public UserFactory asTeacher() {
        this.email = "teacher@test.com";
        this.roles = new HashSet<>(Set.of(RoleFactory.defaultTeacherRole()));
        this.rolesJpa = new HashSet<>(Set.of(RoleFactory.defaultTeacherRoleJpa()));
        return this;
    }

    public UserFactory asAdmin() {
        this.email = "admin@test.com";
        this.roles = new HashSet<>(Set.of(RoleFactory.defaultAdminRole()));
        this.rolesJpa = new HashSet<>(Set.of(RoleFactory.defaultAdminRoleJpa()));
        return this;
    }

    public UserFactory withMultipleRoles(RoleType... roleTypes) {
        Set<Role> multipleRoles = new HashSet<>();
        Set<RoleJpaEntity> multipleRolesJpa = new HashSet<>();

        for (RoleType roleType : roleTypes) {
            switch (roleType) {
                case STUDENT -> {
                    multipleRoles.add(RoleFactory.defaultStudentRole());
                    multipleRolesJpa.add(RoleFactory.defaultStudentRoleJpa());
                }
                case TEACHER -> {
                    multipleRoles.add(RoleFactory.defaultTeacherRole());
                    multipleRolesJpa.add(RoleFactory.defaultTeacherRoleJpa());
                }
                case ADMIN -> {
                    multipleRoles.add(RoleFactory.defaultAdminRole());
                    multipleRolesJpa.add(RoleFactory.defaultAdminRoleJpa());
                }
            }
        }

        this.roles = multipleRoles;
        this.rolesJpa = multipleRolesJpa;
        return this;
    }

    public UserFactory active() {
        this.status = UserStatus.ACTIVE;
        return this;
    }

    public UserFactory pendingActivation() {
        this.status = UserStatus.PENDING_ACTIVATION;
        return this;
    }

    public UserFactory blocked() {
        this.status = UserStatus.BLOCKED;
        return this;
    }

    // ========== Build Methods ==========

    /**
     * Build domain User entity.
     */
    public User buildDomain() {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .status(status)
                .roles(roles)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Build JPA User entity.
     */
    public UserJpaEntity buildJpaEntity() {
        return UserJpaEntity.builder()
                .id(id)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .status(status)
                .roles(rolesJpa)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // ========== Static Shortcuts ==========

    public static User defaultStudent() {
        return builder().asStudent().buildDomain();
    }

    public static User defaultTeacher() {
        return builder().id(2L).asTeacher().buildDomain();
    }

    public static User defaultAdmin() {
        return builder().id(3L).asAdmin().buildDomain();
    }

    public static UserJpaEntity defaultStudentJpa() {
        return builder().asStudent().buildJpaEntity();
    }

    public static UserJpaEntity defaultTeacherJpa() {
        return builder().id(2L).asTeacher().buildJpaEntity();
    }

    public static UserJpaEntity defaultAdminJpa() {
        return builder().id(3L).asAdmin().buildJpaEntity();
    }

    /**
     * Create a user with specific email (useful for uniqueness tests).
     */
    public static User withEmail(String email) {
        return builder().email(email).buildDomain();
    }

    /**
     * Create a blocked student user.
     */
    public static User blockedStudent() {
        return builder().asStudent().blocked().buildDomain();
    }

    /**
     * Create a pending activation student user.
     */
    public static User pendingStudent() {
        return builder().asStudent().pendingActivation().buildDomain();
    }

    /**
     * Create a user with multiple roles (e.g., TEACHER + ADMIN).
     */
    public static User withRoles(RoleType... roleTypes) {
        return builder().withMultipleRoles(roleTypes).buildDomain();
    }
}
