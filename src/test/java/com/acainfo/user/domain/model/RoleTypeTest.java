package com.acainfo.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RoleType enum.
 */
@DisplayName("RoleType Enum Tests")
class RoleTypeTest {

    @Test
    @DisplayName("Should have STUDENT role type")
    void shouldHaveStudentRoleType() {
        // When
        RoleType roleType = RoleType.STUDENT;

        // Then
        assertThat(roleType).isNotNull();
        assertThat(roleType.name()).isEqualTo("STUDENT");
    }

    @Test
    @DisplayName("Should have TEACHER role type")
    void shouldHaveTeacherRoleType() {
        // When
        RoleType roleType = RoleType.TEACHER;

        // Then
        assertThat(roleType).isNotNull();
        assertThat(roleType.name()).isEqualTo("TEACHER");
    }

    @Test
    @DisplayName("Should have ADMIN role type")
    void shouldHaveAdminRoleType() {
        // When
        RoleType roleType = RoleType.ADMIN;

        // Then
        assertThat(roleType).isNotNull();
        assertThat(roleType.name()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Should have exactly 3 role types")
    void shouldHaveExactlyThreeRoleTypes() {
        // When
        RoleType[] roleTypes = RoleType.values();

        // Then
        assertThat(roleTypes).hasSize(3);
    }

    @Test
    @DisplayName("Should be able to get role type by name")
    void shouldGetRoleTypeByName() {
        // When
        RoleType student = RoleType.valueOf("STUDENT");
        RoleType teacher = RoleType.valueOf("TEACHER");
        RoleType admin = RoleType.valueOf("ADMIN");

        // Then
        assertThat(student).isEqualTo(RoleType.STUDENT);
        assertThat(teacher).isEqualTo(RoleType.TEACHER);
        assertThat(admin).isEqualTo(RoleType.ADMIN);
    }

    @Test
    @DisplayName("Should maintain order: STUDENT, TEACHER, ADMIN")
    void shouldMaintainCorrectOrder() {
        // When
        RoleType[] roleTypes = RoleType.values();

        // Then
        assertThat(roleTypes[0]).isEqualTo(RoleType.STUDENT);
        assertThat(roleTypes[1]).isEqualTo(RoleType.TEACHER);
        assertThat(roleTypes[2]).isEqualTo(RoleType.ADMIN);
    }
}
