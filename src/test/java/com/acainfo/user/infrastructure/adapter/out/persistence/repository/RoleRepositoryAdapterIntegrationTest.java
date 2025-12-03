package com.acainfo.user.infrastructure.adapter.out.persistence.repository;

import com.acainfo.shared.config.TestJpaConfig;
import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.infrastructure.mapper.RolePersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RoleRepositoryAdapter with H2 database.
 * Tests persistence layer with real JPA operations.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestJpaConfig.class, RoleRepositoryAdapter.class, RolePersistenceMapper.class})
@DisplayName("RoleRepositoryAdapter Integration Tests")
class RoleRepositoryAdapterIntegrationTest {

    @Autowired
    private RoleRepositoryAdapter roleRepositoryAdapter;

    @Autowired
    private JpaRoleRepository jpaRoleRepository;

    private Role studentRole;
    private Role teacherRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Clean database
        jpaRoleRepository.deleteAll();

        // Create test roles
        studentRole = Role.builder()
                .type(RoleType.STUDENT)
                .description("Student role")
                .build();

        teacherRole = Role.builder()
                .type(RoleType.TEACHER)
                .description("Teacher role")
                .build();

        adminRole = Role.builder()
                .type(RoleType.ADMIN)
                .description("Administrator role")
                .build();
    }

    @Test
    @DisplayName("Should save and retrieve role by ID")
    void save_AndFindById_Success() {
        // When
        Role savedRole = roleRepositoryAdapter.save(studentRole);

        // Then
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getType()).isEqualTo(RoleType.STUDENT);
        assertThat(savedRole.getDescription()).isEqualTo("Student role");

        // Verify retrieval
        Optional<Role> found = roleRepositoryAdapter.findById(savedRole.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(RoleType.STUDENT);
    }

    @Test
    @DisplayName("Should return empty when finding non-existent role by ID")
    void findById_WhenNotFound_ReturnsEmpty() {
        // When
        Optional<Role> found = roleRepositoryAdapter.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find role by type")
    void findByType_WhenExists_ReturnsRole() {
        // Given
        roleRepositoryAdapter.save(studentRole);

        // When
        Optional<Role> found = roleRepositoryAdapter.findByType(RoleType.STUDENT);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(RoleType.STUDENT);
        assertThat(found.get().getDescription()).isEqualTo("Student role");
    }

    @Test
    @DisplayName("Should return empty when finding non-existent role by type")
    void findByType_WhenNotFound_ReturnsEmpty() {
        // When
        Optional<Role> found = roleRepositoryAdapter.findByType(RoleType.STUDENT);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all roles")
    void findAll_ReturnsAllRoles() {
        // Given
        roleRepositoryAdapter.save(studentRole);
        roleRepositoryAdapter.save(teacherRole);
        roleRepositoryAdapter.save(adminRole);

        // When
        List<Role> allRoles = roleRepositoryAdapter.findAll();

        // Then
        assertThat(allRoles).hasSize(3);
        assertThat(allRoles)
                .extracting(Role::getType)
                .containsExactlyInAnyOrder(RoleType.STUDENT, RoleType.TEACHER, RoleType.ADMIN);
    }

    @Test
    @DisplayName("Should return empty list when no roles exist")
    void findAll_WhenNoRoles_ReturnsEmptyList() {
        // When
        List<Role> allRoles = roleRepositoryAdapter.findAll();

        // Then
        assertThat(allRoles).isEmpty();
    }

    @Test
    @DisplayName("Should check if role exists by type")
    void existsByType_WhenExists_ReturnsTrue() {
        // Given
        roleRepositoryAdapter.save(studentRole);

        // When
        boolean exists = roleRepositoryAdapter.existsByType(RoleType.STUDENT);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when role does not exist")
    void existsByType_WhenNotExists_ReturnsFalse() {
        // When
        boolean exists = roleRepositoryAdapter.existsByType(RoleType.STUDENT);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should update existing role")
    void save_WhenUpdating_UpdatesRole() {
        // Given
        Role savedRole = roleRepositoryAdapter.save(studentRole);
        Long roleId = savedRole.getId();

        // When - Update description
        savedRole.setDescription("Updated student role description");
        Role updatedRole = roleRepositoryAdapter.save(savedRole);

        // Then
        assertThat(updatedRole.getId()).isEqualTo(roleId);
        assertThat(updatedRole.getDescription()).isEqualTo("Updated student role description");

        // Verify in database
        Optional<Role> found = roleRepositoryAdapter.findById(roleId);
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Updated student role description");
    }

    @Test
    @DisplayName("Should handle multiple roles with different types")
    void saveMultipleRoles_WithDifferentTypes_AllSavedCorrectly() {
        // When
        Role savedStudent = roleRepositoryAdapter.save(studentRole);
        Role savedTeacher = roleRepositoryAdapter.save(teacherRole);
        Role savedAdmin = roleRepositoryAdapter.save(adminRole);

        // Then
        assertThat(savedStudent.getId()).isNotNull();
        assertThat(savedTeacher.getId()).isNotNull();
        assertThat(savedAdmin.getId()).isNotNull();

        // Verify all can be retrieved
        assertThat(roleRepositoryAdapter.findByType(RoleType.STUDENT)).isPresent();
        assertThat(roleRepositoryAdapter.findByType(RoleType.TEACHER)).isPresent();
        assertThat(roleRepositoryAdapter.findByType(RoleType.ADMIN)).isPresent();
    }

    @Test
    @DisplayName("Should persist role with null description")
    void save_WithNullDescription_Success() {
        // Given
        Role roleWithoutDescription = Role.builder()
                .type(RoleType.STUDENT)
                .description(null)
                .build();

        // When
        Role savedRole = roleRepositoryAdapter.save(roleWithoutDescription);

        // Then
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getType()).isEqualTo(RoleType.STUDENT);
        assertThat(savedRole.getDescription()).isNull();
    }
}
