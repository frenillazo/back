package com.acainfo.user.infrastructure.adapter.out.persistence.repository;

import com.acainfo.user.application.dto.UserFilters;
import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.RoleJpaEntity;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.acainfo.user.infrastructure.mapper.RolePersistenceMapper;
import com.acainfo.user.infrastructure.mapper.UserPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepositoryAdapter with H2 database.
 * Tests persistence layer with real JPA operations including specifications.
 */
@DataJpaTest
@Import({
        UserRepositoryAdapter.class,
        UserPersistenceMapper.class,
        RoleRepositoryAdapter.class,
        RolePersistenceMapper.class
})
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true"
})
@DisplayName("UserRepositoryAdapter Integration Tests")
class UserRepositoryAdapterIntegrationTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private RoleRepositoryAdapter roleRepositoryAdapter;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JpaRoleRepository jpaRoleRepository;

    private Role studentRole;
    private Role teacherRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Clean database
        jpaUserRepository.deleteAll();
        jpaRoleRepository.deleteAll();

        // Create and save roles
        studentRole = roleRepositoryAdapter.save(Role.builder()
                .type(RoleType.STUDENT)
                .description("Student role")
                .build());

        teacherRole = roleRepositoryAdapter.save(Role.builder()
                .type(RoleType.TEACHER)
                .description("Teacher role")
                .build());

        adminRole = roleRepositoryAdapter.save(Role.builder()
                .type(RoleType.ADMIN)
                .description("Administrator role")
                .build());
    }

    @Nested
    @DisplayName("Save and Find Tests")
    class SaveAndFindTests {

        @Test
        @DisplayName("Should save and retrieve user by ID")
        void save_AndFindById_Success() {
            // Given
            User user = User.builder()
                    .email("student@test.com")
                    .password("hashedPassword")
                    .firstName("John")
                    .lastName("Doe")
                    .status(UserStatus.ACTIVE)
                    .roles(Set.of(studentRole))
                    .build();

            // When
            User savedUser = userRepositoryAdapter.save(user);

            // Then
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getEmail()).isEqualTo("student@test.com");
            assertThat(savedUser.getFirstName()).isEqualTo("John");
            assertThat(savedUser.getLastName()).isEqualTo("Doe");
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(savedUser.getRoles()).hasSize(1);
            assertThat(savedUser.isStudent()).isTrue();
            assertThat(savedUser.getCreatedAt()).isNotNull();
            assertThat(savedUser.getUpdatedAt()).isNotNull();

            // Verify retrieval
            Optional<User> found = userRepositoryAdapter.findById(savedUser.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("student@test.com");
        }

        @Test
        @DisplayName("Should return empty when finding non-existent user by ID")
        void findById_WhenNotFound_ReturnsEmpty() {
            // When
            Optional<User> found = userRepositoryAdapter.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should find user by email (case insensitive)")
        void findByEmail_IgnoreCase_Success() {
            // Given
            User user = createUser("john@test.com", "John", "Doe", studentRole);
            userRepositoryAdapter.save(user);

            // When - Search with different case
            Optional<User> foundLower = userRepositoryAdapter.findByEmail("john@test.com");
            Optional<User> foundUpper = userRepositoryAdapter.findByEmail("JOHN@TEST.COM");
            Optional<User> foundMixed = userRepositoryAdapter.findByEmail("JoHn@TeSt.CoM");

            // Then
            assertThat(foundLower).isPresent();
            assertThat(foundUpper).isPresent();
            assertThat(foundMixed).isPresent();
            assertThat(foundLower.get().getEmail()).isEqualTo("john@test.com");
        }

        @Test
        @DisplayName("Should return empty when finding non-existent user by email")
        void findByEmail_WhenNotFound_ReturnsEmpty() {
            // When
            Optional<User> found = userRepositoryAdapter.findByEmail("nonexistent@test.com");

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should check if user exists by email (case insensitive)")
        void existsByEmail_IgnoreCase_ReturnsTrue() {
            // Given
            User user = createUser("exists@test.com", "John", "Doe", studentRole);
            userRepositoryAdapter.save(user);

            // When & Then
            assertThat(userRepositoryAdapter.existsByEmail("exists@test.com")).isTrue();
            assertThat(userRepositoryAdapter.existsByEmail("EXISTS@TEST.COM")).isTrue();
            assertThat(userRepositoryAdapter.existsByEmail("ExIsTs@TeSt.CoM")).isTrue();
        }

        @Test
        @DisplayName("Should return false when user does not exist")
        void existsByEmail_WhenNotExists_ReturnsFalse() {
            // When
            boolean exists = userRepositoryAdapter.existsByEmail("notexists@test.com");

            // Then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update existing user")
        void save_WhenUpdating_UpdatesUser() {
            // Given
            User user = createUser("update@test.com", "John", "Doe", studentRole);
            User savedUser = userRepositoryAdapter.save(user);
            Long userId = savedUser.getId();

            // When - Update user
            savedUser.setFirstName("Jane");
            savedUser.setLastName("Smith");
            savedUser.setStatus(UserStatus.BLOCKED);
            User updatedUser = userRepositoryAdapter.save(savedUser);

            // Then
            assertThat(updatedUser.getId()).isEqualTo(userId);
            assertThat(updatedUser.getFirstName()).isEqualTo("Jane");
            assertThat(updatedUser.getLastName()).isEqualTo("Smith");
            assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.BLOCKED);

            // Verify in database
            Optional<User> found = userRepositoryAdapter.findById(userId);
            assertThat(found).isPresent();
            assertThat(found.get().getFirstName()).isEqualTo("Jane");
            assertThat(found.get().getStatus()).isEqualTo(UserStatus.BLOCKED);
        }

        @Test
        @DisplayName("Should update user roles")
        void save_WhenUpdatingRoles_UpdatesRoles() {
            // Given
            User user = createUser("teacher@test.com", "Jane", "Smith", studentRole);
            User savedUser = userRepositoryAdapter.save(user);

            // When - Add teacher role
            savedUser.setRoles(Set.of(studentRole, teacherRole));
            User updatedUser = userRepositoryAdapter.save(savedUser);

            // Then
            assertThat(updatedUser.getRoles()).hasSize(2);
            assertThat(updatedUser.isStudent()).isTrue();
            assertThat(updatedUser.isTeacher()).isTrue();
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete user by ID")
        void deleteById_RemovesUser() {
            // Given
            User user = createUser("delete@test.com", "John", "Doe", studentRole);
            User savedUser = userRepositoryAdapter.save(user);
            Long userId = savedUser.getId();

            // When
            userRepositoryAdapter.deleteById(userId);

            // Then
            Optional<User> found = userRepositoryAdapter.findById(userId);
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should delete user by entity")
        void delete_RemovesUser() {
            // Given
            User user = createUser("delete2@test.com", "Jane", "Doe", studentRole);
            User savedUser = userRepositoryAdapter.save(user);
            Long userId = savedUser.getId();

            // When
            userRepositoryAdapter.delete(savedUser);

            // Then
            Optional<User> found = userRepositoryAdapter.findById(userId);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Filter and Specification Tests")
    class FilterTests {

        @BeforeEach
        void setUpUsers() {
            // Create test users
            userRepositoryAdapter.save(createUser("alice@test.com", "Alice", "Anderson", studentRole, UserStatus.ACTIVE));
            userRepositoryAdapter.save(createUser("bob@test.com", "Bob", "Brown", teacherRole, UserStatus.ACTIVE));
            userRepositoryAdapter.save(createUser("charlie@test.com", "Charlie", "Clark", studentRole, UserStatus.BLOCKED));
            userRepositoryAdapter.save(createUser("david@test.com", "David", "Davis", adminRole, UserStatus.ACTIVE));
            userRepositoryAdapter.save(createUser("eve@test.com", "Eve", "Evans", studentRole, UserStatus.PENDING_ACTIVATION));
        }

        @Test
        @DisplayName("Should filter users by email")
        void findWithFilters_ByEmail_ReturnsMatchingUsers() {
            // Given
            UserFilters filters = new UserFilters(
                    "alice@test.com",
                    null,
                    null,
                    null,
                    0,
                    10,
                    "email",
                    "ASC"
            );

            // When
            Page<User> result = userRepositoryAdapter.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("alice@test.com");
        }

        @Test
        @DisplayName("Should filter users by status")
        void findWithFilters_ByStatus_ReturnsMatchingUsers() {
            // Given
            UserFilters filters = new UserFilters(
                    null,
                    null,
                    UserStatus.ACTIVE,
                    null,
                    0,
                    10,
                    "email",
                    "ASC"
            );

            // When
            Page<User> result = userRepositoryAdapter.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).hasSize(3); // alice, bob, david
            assertThat(result.getContent())
                    .allMatch(user -> user.getStatus() == UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should filter users by role type")
        void findWithFilters_ByRoleType_ReturnsMatchingUsers() {
            // Given
            UserFilters filters = new UserFilters(
                    null,
                    null,
                    null,
                    RoleType.STUDENT,
                    0,
                    10,
                    "email",
                    "ASC"
            );

            // When
            Page<User> result = userRepositoryAdapter.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).hasSize(3); // alice, charlie, eve
            assertThat(result.getContent())
                    .allMatch(User::isStudent);
        }

        @Test
        @DisplayName("Should filter users by search term (name)")
        void findWithFilters_BySearchTerm_ReturnsMatchingUsers() {
            // Given
            UserFilters filters = new UserFilters(
                    null,
                    "alice",
                    null,
                    null,
                    0,
                    10,
                    "firstName",
                    "ASC"
            );

            // When
            Page<User> result = userRepositoryAdapter.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Should apply multiple filters together")
        void findWithFilters_WithMultipleFilters_ReturnsMatchingUsers() {
            // Given
            UserFilters filters = new UserFilters(
                    null,
                    null,
                    UserStatus.ACTIVE,
                    RoleType.STUDENT,
                    0,
                    10,
                    "email",
                    "ASC"
            );

            // When
            Page<User> result = userRepositoryAdapter.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).hasSize(1); // Only alice
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("alice@test.com");
        }

        @Test
        @DisplayName("Should support pagination")
        void findWithFilters_WithPagination_ReturnsPagedResults() {
            // Given - Page 0, size 2
            UserFilters filters = new UserFilters(
                    null,
                    null,
                    null,
                    null,
                    0,
                    2,
                    "email",
                    "ASC"
            );

            // When
            Page<User> page0 = userRepositoryAdapter.findWithFilters(filters);

            // Then
            assertThat(page0.getContent()).hasSize(2);
            assertThat(page0.getTotalElements()).isEqualTo(5);
            assertThat(page0.getTotalPages()).isEqualTo(3);
            assertThat(page0.getNumber()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should support sorting ascending")
        void findWithFilters_SortAscending_ReturnsSortedResults() {
            // Given
            UserFilters filters = new UserFilters(
                    null,
                    null,
                    null,
                    RoleType.STUDENT,
                    0,
                    10,
                    "firstName",
                    "ASC"
            );

            // When
            Page<User> result = userRepositoryAdapter.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Alice");
            assertThat(result.getContent().get(1).getFirstName()).isEqualTo("Charlie");
            assertThat(result.getContent().get(2).getFirstName()).isEqualTo("Eve");
        }

        @Test
        @DisplayName("Should support sorting descending")
        void findWithFilters_SortDescending_ReturnsSortedResults() {
            // Given
            UserFilters filters = new UserFilters(
                    null,
                    null,
                    null,
                    RoleType.STUDENT,
                    0,
                    10,
                    "firstName",
                    "DESC"
            );

            // When
            Page<User> result = userRepositoryAdapter.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Eve");
            assertThat(result.getContent().get(1).getFirstName()).isEqualTo("Charlie");
            assertThat(result.getContent().get(2).getFirstName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Should return empty page when no users match filters")
        void findWithFilters_NoMatches_ReturnsEmptyPage() {
            // Given
            UserFilters filters = new UserFilters(
                    "nonexistent@test.com",
                    null,
                    null,
                    null,
                    0,
                    10,
                    "email",
                    "ASC"
            );

            // When
            Page<User> result = userRepositoryAdapter.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("User with Multiple Roles Tests")
    class MultipleRolesTests {

        @Test
        @DisplayName("Should save and retrieve user with multiple roles")
        void save_UserWithMultipleRoles_Success() {
            // Given
            User user = User.builder()
                    .email("multirole@test.com")
                    .password("hashedPassword")
                    .firstName("Multi")
                    .lastName("Role")
                    .status(UserStatus.ACTIVE)
                    .roles(Set.of(teacherRole, adminRole))
                    .build();

            // When
            User savedUser = userRepositoryAdapter.save(user);

            // Then
            assertThat(savedUser.getRoles()).hasSize(2);
            assertThat(savedUser.isTeacher()).isTrue();
            assertThat(savedUser.isAdmin()).isTrue();
            assertThat(savedUser.isStudent()).isFalse();

            // Verify retrieval
            Optional<User> found = userRepositoryAdapter.findById(savedUser.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getRoles()).hasSize(2);
        }
    }

    // Helper methods
    private User createUser(String email, String firstName, String lastName, Role role) {
        return createUser(email, firstName, lastName, role, UserStatus.ACTIVE);
    }

    private User createUser(String email, String firstName, String lastName, Role role, UserStatus status) {
        return User.builder()
                .email(email)
                .password("hashedPassword")
                .firstName(firstName)
                .lastName(lastName)
                .status(status)
                .roles(Set.of(role))
                .build();
    }
}
