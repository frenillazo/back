package com.acainfo.user.application.service;

import com.acainfo.shared.factory.RoleFactory;
import com.acainfo.shared.factory.UserFactory;
import com.acainfo.user.application.dto.CreateTeacherCommand;
import com.acainfo.user.application.dto.UpdateTeacherCommand;
import com.acainfo.user.application.dto.UserFilters;
import com.acainfo.user.application.port.out.RoleRepositoryPort;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.DuplicateEmailException;
import com.acainfo.user.domain.exception.UserNotFoundException;
import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TeacherService with Mockito.
 * Tests teacher management logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService Tests")
class TeacherServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TeacherService teacherService;

    private User testTeacher;
    private Role teacherRole;

    @BeforeEach
    void setUp() {
        teacherRole = RoleFactory.defaultTeacherRole();
        testTeacher = UserFactory.defaultTeacher();
    }

    @Nested
    @DisplayName("Create Teacher Tests")
    class CreateTeacherTests {

        @Test
        @DisplayName("Should create teacher successfully")
        void createTeacher_WithValidData_CreatesTeacher() {
            // Given
            CreateTeacherCommand command = new CreateTeacherCommand(
                    "teacher@test.com",
                    "password123",
                    "Jane",
                    "Smith"
            );
            String encodedPassword = "encodedPassword";

            when(userRepositoryPort.existsByEmail("teacher@test.com")).thenReturn(false);
            when(roleRepositoryPort.findByType(RoleType.TEACHER)).thenReturn(Optional.of(teacherRole));
            when(passwordEncoder.encode(command.password())).thenReturn(encodedPassword);
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return user.toBuilder().id(1L).build();
            });

            // When
            User result = teacherService.createTeacher(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("teacher@test.com");
            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getLastName()).isEqualTo("Smith");
            assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(result.isTeacher()).isTrue();

            verify(userRepositoryPort).existsByEmail("teacher@test.com");
            verify(roleRepositoryPort).findByType(RoleType.TEACHER);
            verify(passwordEncoder).encode(command.password());
            verify(userRepositoryPort).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw DuplicateEmailException when email exists")
        void createTeacher_WithDuplicateEmail_ThrowsException() {
            // Given
            CreateTeacherCommand command = new CreateTeacherCommand(
                    "existing@test.com",
                    "password123",
                    "Jane",
                    "Smith"
            );

            when(userRepositoryPort.existsByEmail("existing@test.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> teacherService.createTeacher(command))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("existing@test.com");

            verify(userRepositoryPort).existsByEmail("existing@test.com");
            verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException with short password")
        void createTeacher_WithShortPassword_ThrowsException() {
            // Given
            CreateTeacherCommand command = new CreateTeacherCommand(
                    "teacher@test.com",
                    "123",
                    "Jane",
                    "Smith"
            );

            when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> teacherService.createTeacher(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least 6 characters");

            verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should normalize email to lowercase and trim")
        void createTeacher_WithMixedCaseEmail_NormalizesEmail() {
            // Given
            CreateTeacherCommand command = new CreateTeacherCommand(
                    "  TEACHER@TEST.COM  ",
                    "password123",
                    "Jane",
                    "Smith"
            );

            when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
            when(roleRepositoryPort.findByType(RoleType.TEACHER)).thenReturn(Optional.of(teacherRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = teacherService.createTeacher(command);

            // Then
            assertThat(result.getEmail()).isEqualTo("teacher@test.com");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when TEACHER role not found")
        void createTeacher_WhenTeacherRoleNotFound_ThrowsException() {
            // Given
            CreateTeacherCommand command = new CreateTeacherCommand(
                    "teacher@test.com",
                    "password123",
                    "Jane",
                    "Smith"
            );

            when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
            when(roleRepositoryPort.findByType(RoleType.TEACHER)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> teacherService.createTeacher(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("TEACHER role not found");
        }
    }

    @Nested
    @DisplayName("Update Teacher Tests")
    class UpdateTeacherTests {

        @Test
        @DisplayName("Should update teacher successfully")
        void updateTeacher_WithValidData_UpdatesTeacher() {
            // Given
            Long teacherId = 1L;
            UpdateTeacherCommand command = new UpdateTeacherCommand("Janet", "Smithson");

            when(userRepositoryPort.findById(teacherId)).thenReturn(Optional.of(testTeacher));
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = teacherService.updateTeacher(teacherId, command);

            // Then
            assertThat(result.getFirstName()).isEqualTo("Janet");
            assertThat(result.getLastName()).isEqualTo("Smithson");
            verify(userRepositoryPort).findById(teacherId);
            verify(userRepositoryPort).save(any(User.class));
        }

        @Test
        @DisplayName("Should trim whitespace from names")
        void updateTeacher_WithWhitespace_TrimsNames() {
            // Given
            Long teacherId = 1L;
            UpdateTeacherCommand command = new UpdateTeacherCommand("  Janet  ", "  Smithson  ");

            when(userRepositoryPort.findById(teacherId)).thenReturn(Optional.of(testTeacher));
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            teacherService.updateTeacher(teacherId, command);

            // Then
            verify(userRepositoryPort).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when teacher not found")
        void updateTeacher_WhenTeacherNotFound_ThrowsException() {
            // Given
            Long teacherId = 999L;
            UpdateTeacherCommand command = new UpdateTeacherCommand("Janet", "Smithson");

            when(userRepositoryPort.findById(teacherId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> teacherService.updateTeacher(teacherId, command))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepositoryPort).findById(teacherId);
            verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user is not a teacher")
        void updateTeacher_WhenUserIsNotTeacher_ThrowsException() {
            // Given
            Long userId = 1L;
            UpdateTeacherCommand command = new UpdateTeacherCommand("Janet", "Smithson");
            User studentUser = UserFactory.defaultStudent();

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(studentUser));

            // When & Then
            assertThatThrownBy(() -> teacherService.updateTeacher(userId, command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not a teacher");

            verify(userRepositoryPort, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Delete Teacher Tests")
    class DeleteTeacherTests {

        @Test
        @DisplayName("Should delete (block) teacher successfully")
        void deleteTeacher_WithValidTeacher_BlocksTeacher() {
            // Given
            Long teacherId = 1L;

            when(userRepositoryPort.findById(teacherId)).thenReturn(Optional.of(testTeacher));
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            teacherService.deleteTeacher(teacherId);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepositoryPort).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.BLOCKED);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when teacher not found")
        void deleteTeacher_WhenTeacherNotFound_ThrowsException() {
            // Given
            Long teacherId = 999L;

            when(userRepositoryPort.findById(teacherId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> teacherService.deleteTeacher(teacherId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepositoryPort).findById(teacherId);
            verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user is not a teacher")
        void deleteTeacher_WhenUserIsNotTeacher_ThrowsException() {
            // Given
            Long userId = 1L;
            User studentUser = UserFactory.defaultStudent();

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(studentUser));

            // When & Then
            assertThatThrownBy(() -> teacherService.deleteTeacher(userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not a teacher");

            verify(userRepositoryPort, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Get Teachers Tests")
    class GetTeachersTests {

        @Test
        @DisplayName("Should get teachers with filters")
        void getTeachers_WithFilters_ReturnsTeachersPage() {
            // Given
            UserFilters filters = new UserFilters(
                    null,
                    "john",
                    null,
                    null, // RoleType will be forced to TEACHER
                    0,
                    10,
                    "firstName",
                    "ASC"
            );

            Page<User> expectedPage = new PageImpl<>(List.of(testTeacher));
            when(userRepositoryPort.findWithFilters(any(UserFilters.class))).thenReturn(expectedPage);

            // When
            Page<User> result = teacherService.getTeachers(filters);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(testTeacher);

            // Verify that roleType was forced to TEACHER
            ArgumentCaptor<UserFilters> filtersCaptor = ArgumentCaptor.forClass(UserFilters.class);
            verify(userRepositoryPort).findWithFilters(filtersCaptor.capture());
            UserFilters capturedFilters = filtersCaptor.getValue();
            assertThat(capturedFilters.roleType()).isEqualTo(RoleType.TEACHER);
        }

        @Test
        @DisplayName("Should force TEACHER role even if other role specified")
        void getTeachers_WithDifferentRoleFilter_ForcesTeacherRole() {
            // Given
            UserFilters filters = new UserFilters(
                    null,
                    null,
                    null,
                    RoleType.STUDENT, // Will be overridden
                    0,
                    10,
                    null,
                    null
            );

            Page<User> expectedPage = new PageImpl<>(List.of(testTeacher));
            when(userRepositoryPort.findWithFilters(any(UserFilters.class))).thenReturn(expectedPage);

            // When
            teacherService.getTeachers(filters);

            // Then
            ArgumentCaptor<UserFilters> filtersCaptor = ArgumentCaptor.forClass(UserFilters.class);
            verify(userRepositoryPort).findWithFilters(filtersCaptor.capture());
            UserFilters capturedFilters = filtersCaptor.getValue();
            assertThat(capturedFilters.roleType()).isEqualTo(RoleType.TEACHER);
        }
    }

    @Nested
    @DisplayName("Get Teacher By ID Tests")
    class GetTeacherByIdTests {

        @Test
        @DisplayName("Should get teacher by ID successfully")
        void getTeacherById_WhenTeacherExists_ReturnsTeacher() {
            // Given
            Long teacherId = 1L;
            when(userRepositoryPort.findById(teacherId)).thenReturn(Optional.of(testTeacher));

            // When
            User result = teacherService.getTeacherById(teacherId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testTeacher.getId());
            assertThat(result.isTeacher()).isTrue();
            verify(userRepositoryPort).findById(teacherId);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when teacher not found")
        void getTeacherById_WhenTeacherNotFound_ThrowsException() {
            // Given
            Long teacherId = 999L;
            when(userRepositoryPort.findById(teacherId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> teacherService.getTeacherById(teacherId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepositoryPort).findById(teacherId);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user is not a teacher")
        void getTeacherById_WhenUserIsNotTeacher_ThrowsException() {
            // Given
            Long userId = 1L;
            User studentUser = UserFactory.defaultStudent();

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(studentUser));

            // When & Then
            assertThatThrownBy(() -> teacherService.getTeacherById(userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not a teacher");
        }
    }
}
