package com.acainfo.shared.factory;

import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.RoleJpaEntity;

/**
 * Factory for creating Role test data.
 * Provides fluent API for building Role domain and JPA entities with sensible defaults.
 */
public class RoleFactory {

    private Long id;
    private RoleType type;
    private String description;

    private RoleFactory() {
        // Default values
        this.id = 1L;
        this.type = RoleType.STUDENT;
        this.description = "Student role";
    }

    public static RoleFactory builder() {
        return new RoleFactory();
    }

    public RoleFactory id(Long id) {
        this.id = id;
        return this;
    }

    public RoleFactory type(RoleType type) {
        this.type = type;
        return this;
    }

    public RoleFactory description(String description) {
        this.description = description;
        return this;
    }

    // ========== Convenience Methods ==========

    public RoleFactory asStudent() {
        this.type = RoleType.STUDENT;
        this.description = "Student role";
        return this;
    }

    public RoleFactory asTeacher() {
        this.type = RoleType.TEACHER;
        this.description = "Teacher role";
        return this;
    }

    public RoleFactory asAdmin() {
        this.type = RoleType.ADMIN;
        this.description = "Administrator role";
        return this;
    }

    // ========== Build Methods ==========

    /**
     * Build domain Role entity.
     */
    public Role buildDomain() {
        return Role.builder()
                .id(id)
                .type(type)
                .description(description)
                .build();
    }

    /**
     * Build JPA Role entity.
     */
    public RoleJpaEntity buildJpaEntity() {
        return RoleJpaEntity.builder()
                .id(id)
                .type(type)
                .description(description)
                .build();
    }

    // ========== Static Shortcuts ==========

    public static Role defaultStudentRole() {
        return builder().asStudent().buildDomain();
    }

    public static Role defaultTeacherRole() {
        return builder().id(2L).asTeacher().buildDomain();
    }

    public static Role defaultAdminRole() {
        return builder().id(3L).asAdmin().buildDomain();
    }

    public static RoleJpaEntity defaultStudentRoleJpa() {
        return builder().asStudent().buildJpaEntity();
    }

    public static RoleJpaEntity defaultTeacherRoleJpa() {
        return builder().id(2L).asTeacher().buildJpaEntity();
    }

    public static RoleJpaEntity defaultAdminRoleJpa() {
        return builder().id(3L).asAdmin().buildJpaEntity();
    }
}
