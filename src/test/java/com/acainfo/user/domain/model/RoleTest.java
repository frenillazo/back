package com.acainfo.user.domain.model;

import com.acainfo.shared.factory.RoleFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Role domain entity.
 * Tests query methods and business logic (no Spring context).
 */
@DisplayName("Role Domain Tests")
class RoleTest {

    @Test
    @DisplayName("Should identify admin role correctly")
    void isAdmin_WhenRoleTypeIsAdmin_ReturnsTrue() {
        // Given
        Role adminRole = RoleFactory.builder()
                .type(RoleType.ADMIN)
                .buildDomain();

        // When
        boolean result = adminRole.isAdmin();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not identify non-admin role as admin")
    void isAdmin_WhenRoleTypeIsNotAdmin_ReturnsFalse() {
        // Given
        Role studentRole = RoleFactory.builder()
                .type(RoleType.STUDENT)
                .buildDomain();

        // When
        boolean result = studentRole.isAdmin();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should identify teacher role correctly")
    void isTeacher_WhenRoleTypeIsTeacher_ReturnsTrue() {
        // Given
        Role teacherRole = RoleFactory.builder()
                .type(RoleType.TEACHER)
                .buildDomain();

        // When
        boolean result = teacherRole.isTeacher();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not identify non-teacher role as teacher")
    void isTeacher_WhenRoleTypeIsNotTeacher_ReturnsFalse() {
        // Given
        Role studentRole = RoleFactory.builder()
                .type(RoleType.STUDENT)
                .buildDomain();

        // When
        boolean result = studentRole.isTeacher();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should identify student role correctly")
    void isStudent_WhenRoleTypeIsStudent_ReturnsTrue() {
        // Given
        Role studentRole = RoleFactory.builder()
                .type(RoleType.STUDENT)
                .buildDomain();

        // When
        boolean result = studentRole.isStudent();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not identify non-student role as student")
    void isStudent_WhenRoleTypeIsNotStudent_ReturnsFalse() {
        // Given
        Role adminRole = RoleFactory.builder()
                .type(RoleType.ADMIN)
                .buildDomain();

        // When
        boolean result = adminRole.isStudent();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should consider roles equal when type is the same")
    void equals_WhenSameType_ReturnsTrue() {
        // Given
        Role role1 = RoleFactory.builder()
                .id(1L)
                .type(RoleType.STUDENT)
                .description("Student role")
                .buildDomain();

        Role role2 = RoleFactory.builder()
                .id(2L) // Different ID
                .type(RoleType.STUDENT) // Same type
                .description("Different description") // Different description
                .buildDomain();

        // When & Then
        assertThat(role1).isEqualTo(role2);
        assertThat(role1.hashCode()).isEqualTo(role2.hashCode());
    }

    @Test
    @DisplayName("Should consider roles different when type differs")
    void equals_WhenDifferentType_ReturnsFalse() {
        // Given
        Role studentRole = RoleFactory.builder()
                .type(RoleType.STUDENT)
                .buildDomain();

        Role teacherRole = RoleFactory.builder()
                .type(RoleType.TEACHER)
                .buildDomain();

        // When & Then
        assertThat(studentRole).isNotEqualTo(teacherRole);
    }

    @Test
    @DisplayName("Should build role with all properties using builder")
    void builder_WithAllProperties_CreatesRoleCorrectly() {
        // Given & When
        Role role = Role.builder()
                .id(1L)
                .type(RoleType.ADMIN)
                .description("Administrator role")
                .build();

        // Then
        assertThat(role.getId()).isEqualTo(1L);
        assertThat(role.getType()).isEqualTo(RoleType.ADMIN);
        assertThat(role.getDescription()).isEqualTo("Administrator role");
    }

    @Test
    @DisplayName("Should use factory shortcuts correctly")
    void factoryShortcuts_CreateCorrectRoles() {
        // When
        Role studentRole = RoleFactory.defaultStudentRole();
        Role teacherRole = RoleFactory.defaultTeacherRole();
        Role adminRole = RoleFactory.defaultAdminRole();

        // Then
        assertThat(studentRole.getType()).isEqualTo(RoleType.STUDENT);
        assertThat(studentRole.isStudent()).isTrue();
        assertThat(studentRole.isTeacher()).isFalse();
        assertThat(studentRole.isAdmin()).isFalse();

        assertThat(teacherRole.getType()).isEqualTo(RoleType.TEACHER);
        assertThat(teacherRole.isTeacher()).isTrue();
        assertThat(teacherRole.isStudent()).isFalse();
        assertThat(teacherRole.isAdmin()).isFalse();

        assertThat(adminRole.getType()).isEqualTo(RoleType.ADMIN);
        assertThat(adminRole.isAdmin()).isTrue();
        assertThat(adminRole.isStudent()).isFalse();
        assertThat(adminRole.isTeacher()).isFalse();
    }
}
