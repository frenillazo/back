package com.acainfo.user.domain.model;

import com.acainfo.shared.factory.RoleFactory;
import com.acainfo.shared.factory.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for User domain entity.
 * Tests query methods and business logic (no Spring context).
 */
@DisplayName("User Domain Tests")
class UserTest {

    @Nested
    @DisplayName("Role Identification Tests")
    class RoleIdentificationTests {

        @Test
        @DisplayName("Should identify admin user correctly")
        void isAdmin_WhenUserHasAdminRole_ReturnsTrue() {
            // Given
            User user = UserFactory.builder()
                    .roles(Set.of(RoleFactory.defaultAdminRole()))
                    .buildDomain();

            // When
            boolean result = user.isAdmin();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-admin user as admin")
        void isAdmin_WhenUserDoesNotHaveAdminRole_ReturnsFalse() {
            // Given
            User user = UserFactory.builder()
                    .roles(Set.of(RoleFactory.defaultStudentRole()))
                    .buildDomain();

            // When
            boolean result = user.isAdmin();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should identify teacher user correctly")
        void isTeacher_WhenUserHasTeacherRole_ReturnsTrue() {
            // Given
            User user = UserFactory.builder()
                    .roles(Set.of(RoleFactory.defaultTeacherRole()))
                    .buildDomain();

            // When
            boolean result = user.isTeacher();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-teacher user as teacher")
        void isTeacher_WhenUserDoesNotHaveTeacherRole_ReturnsFalse() {
            // Given
            User user = UserFactory.builder()
                    .roles(Set.of(RoleFactory.defaultStudentRole()))
                    .buildDomain();

            // When
            boolean result = user.isTeacher();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should identify student user correctly")
        void isStudent_WhenUserHasStudentRole_ReturnsTrue() {
            // Given
            User user = UserFactory.builder()
                    .roles(Set.of(RoleFactory.defaultStudentRole()))
                    .buildDomain();

            // When
            boolean result = user.isStudent();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-student user as student")
        void isStudent_WhenUserDoesNotHaveStudentRole_ReturnsFalse() {
            // Given
            User user = UserFactory.builder()
                    .roles(Set.of(RoleFactory.defaultAdminRole()))
                    .buildDomain();

            // When
            boolean result = user.isStudent();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should identify user with multiple roles correctly")
        void multipleRoles_UserHasBothTeacherAndAdminRoles_BothReturnTrue() {
            // Given
            User user = UserFactory.builder()
                    .roles(Set.of(
                            RoleFactory.defaultTeacherRole(),
                            RoleFactory.defaultAdminRole()
                    ))
                    .buildDomain();

            // When & Then
            assertThat(user.isTeacher()).isTrue();
            assertThat(user.isAdmin()).isTrue();
            assertThat(user.isStudent()).isFalse();
        }

        @Test
        @DisplayName("Should handle user with no roles")
        void noRoles_AllRoleChecksReturnFalse() {
            // Given
            User user = UserFactory.builder()
                    .roles(Set.of())
                    .buildDomain();

            // When & Then
            assertThat(user.isAdmin()).isFalse();
            assertThat(user.isTeacher()).isFalse();
            assertThat(user.isStudent()).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should identify active user correctly")
        void isActive_WhenStatusIsActive_ReturnsTrue() {
            // Given
            User user = UserFactory.builder()
                    .status(UserStatus.ACTIVE)
                    .buildDomain();

            // When
            boolean result = user.isActive();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-active user as active")
        void isActive_WhenStatusIsNotActive_ReturnsFalse() {
            // Given
            User user = UserFactory.builder()
                    .status(UserStatus.PENDING_ACTIVATION)
                    .buildDomain();

            // When
            boolean result = user.isActive();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should identify blocked user correctly")
        void isBlocked_WhenStatusIsBlocked_ReturnsTrue() {
            // Given
            User user = UserFactory.builder()
                    .status(UserStatus.BLOCKED)
                    .buildDomain();

            // When
            boolean result = user.isBlocked();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify non-blocked user as blocked")
        void isBlocked_WhenStatusIsNotBlocked_ReturnsFalse() {
            // Given
            User user = UserFactory.builder()
                    .status(UserStatus.ACTIVE)
                    .buildDomain();

            // When
            boolean result = user.isBlocked();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Full Name Tests")
    class FullNameTests {

        @Test
        @DisplayName("Should return full name correctly")
        void getFullName_ReturnsFirstNamePlusLastName() {
            // Given
            User user = UserFactory.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .buildDomain();

            // When
            String fullName = user.getFullName();

            // Then
            assertThat(fullName).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should handle names with spaces")
        void getFullName_WithNamesContainingSpaces_ReturnsCorrectly() {
            // Given
            User user = UserFactory.builder()
                    .firstName("Mary Jane")
                    .lastName("Watson Parker")
                    .buildDomain();

            // When
            String fullName = user.getFullName();

            // Then
            assertThat(fullName).isEqualTo("Mary Jane Watson Parker");
        }
    }

    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should consider users equal when email is the same")
        void equals_WhenSameEmail_ReturnsTrue() {
            // Given
            User user1 = UserFactory.builder()
                    .id(1L)
                    .email("john@test.com")
                    .firstName("John")
                    .buildDomain();

            User user2 = UserFactory.builder()
                    .id(2L) // Different ID
                    .email("john@test.com") // Same email
                    .firstName("Jane") // Different name
                    .buildDomain();

            // When & Then
            assertThat(user1).isEqualTo(user2);
            assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        }

        @Test
        @DisplayName("Should consider users different when email differs")
        void equals_WhenDifferentEmail_ReturnsFalse() {
            // Given
            User user1 = UserFactory.builder()
                    .email("john@test.com")
                    .buildDomain();

            User user2 = UserFactory.builder()
                    .email("jane@test.com")
                    .buildDomain();

            // When & Then
            assertThat(user1).isNotEqualTo(user2);
        }

        @Test
        @DisplayName("Should handle null comparison")
        void equals_WhenComparedToNull_ReturnsFalse() {
            // Given
            User user = UserFactory.defaultStudent();

            // When & Then
            assertThat(user).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should handle comparison to different class")
        void equals_WhenComparedToDifferentClass_ReturnsFalse() {
            // Given
            User user = UserFactory.defaultStudent();
            String notAUser = "Not a user";

            // When & Then
            assertThat(user).isNotEqualTo(notAUser);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build user with all properties")
        void builder_WithAllProperties_CreatesUserCorrectly() {
            // Given
            Role studentRole = RoleFactory.defaultStudentRole();

            // When
            User user = User.builder()
                    .id(1L)
                    .email("student@test.com")
                    .password("encodedPassword")
                    .firstName("John")
                    .lastName("Doe")
                    .status(UserStatus.ACTIVE)
                    .roles(Set.of(studentRole))
                    .build();

            // Then
            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getEmail()).isEqualTo("student@test.com");
            assertThat(user.getPassword()).isEqualTo("encodedPassword");
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.getRoles()).containsExactly(studentRole);
        }

        @Test
        @DisplayName("Should initialize roles as empty set by default")
        void builder_WithoutRoles_InitializesEmptySet() {
            // Given & When
            User user = User.builder()
                    .email("test@test.com")
                    .build();

            // Then
            assertThat(user.getRoles()).isNotNull();
            assertThat(user.getRoles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Factory Tests")
    class FactoryTests {

        @Test
        @DisplayName("Should create default student with correct properties")
        void defaultStudent_HasStudentRoleAndActiveStatus() {
            // When
            User student = UserFactory.defaultStudent();

            // Then
            assertThat(student.getEmail()).isEqualTo("student@test.com");
            assertThat(student.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(student.isStudent()).isTrue();
            assertThat(student.isTeacher()).isFalse();
            assertThat(student.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("Should create default teacher with correct properties")
        void defaultTeacher_HasTeacherRoleAndActiveStatus() {
            // When
            User teacher = UserFactory.defaultTeacher();

            // Then
            assertThat(teacher.getEmail()).isEqualTo("teacher@test.com");
            assertThat(teacher.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(teacher.isTeacher()).isTrue();
            assertThat(teacher.isStudent()).isFalse();
            assertThat(teacher.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("Should create default admin with correct properties")
        void defaultAdmin_HasAdminRoleAndActiveStatus() {
            // When
            User admin = UserFactory.defaultAdmin();

            // Then
            assertThat(admin.getEmail()).isEqualTo("admin@test.com");
            assertThat(admin.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(admin.isAdmin()).isTrue();
            assertThat(admin.isStudent()).isFalse();
            assertThat(admin.isTeacher()).isFalse();
        }

        @Test
        @DisplayName("Should create blocked student correctly")
        void blockedStudent_HasBlockedStatus() {
            // When
            User blockedStudent = UserFactory.blockedStudent();

            // Then
            assertThat(blockedStudent.isBlocked()).isTrue();
            assertThat(blockedStudent.isActive()).isFalse();
            assertThat(blockedStudent.isStudent()).isTrue();
        }

        @Test
        @DisplayName("Should create pending student correctly")
        void pendingStudent_HasPendingActivationStatus() {
            // When
            User pendingStudent = UserFactory.pendingStudent();

            // Then
            assertThat(pendingStudent.getStatus()).isEqualTo(UserStatus.PENDING_ACTIVATION);
            assertThat(pendingStudent.isActive()).isFalse();
            assertThat(pendingStudent.isStudent()).isTrue();
        }

        @Test
        @DisplayName("Should create user with multiple roles correctly")
        void withRoles_CreatesUserWithSpecifiedRoles() {
            // When
            User user = UserFactory.withRoles(RoleType.TEACHER, RoleType.ADMIN);

            // Then
            assertThat(user.isTeacher()).isTrue();
            assertThat(user.isAdmin()).isTrue();
            assertThat(user.isStudent()).isFalse();
            assertThat(user.getRoles()).hasSize(2);
        }

        @Test
        @DisplayName("Should create user with custom email")
        void withEmail_CreatesUserWithSpecifiedEmail() {
            // Given
            String customEmail = "custom@example.com";

            // When
            User user = UserFactory.withEmail(customEmail);

            // Then
            assertThat(user.getEmail()).isEqualTo(customEmail);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should not include password in toString")
        void toString_DoesNotIncludePassword() {
            // Given
            User user = UserFactory.builder()
                    .email("test@test.com")
                    .password("secretPassword123")
                    .buildDomain();

            // When
            String userString = user.toString();

            // Then
            assertThat(userString).doesNotContain("secretPassword123");
            assertThat(userString).doesNotContain("password");
        }

        @Test
        @DisplayName("Should include email in toString")
        void toString_IncludesEmail() {
            // Given
            User user = UserFactory.builder()
                    .email("test@test.com")
                    .buildDomain();

            // When
            String userString = user.toString();

            // Then
            assertThat(userString).contains("test@test.com");
        }
    }
}
