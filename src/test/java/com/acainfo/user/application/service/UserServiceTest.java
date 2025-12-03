package com.acainfo.user.application.service;

import com.acainfo.shared.factory.UserFactory;
import com.acainfo.user.application.dto.UpdateUserCommand;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.InvalidCredentialsException;
import com.acainfo.user.domain.exception.UserNotFoundException;
import com.acainfo.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService with Mockito.
 * Tests business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = UserFactory.defaultStudent();
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when found by ID")
        void getUserById_WhenUserExists_ReturnsUser() {
            // Given
            Long userId = 1L;
            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(testUser));

            // When
            User result = userService.getUserById(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testUser.getId());
            assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
            verify(userRepositoryPort).findById(userId);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void getUserById_WhenUserNotFound_ThrowsException() {
            // Given
            Long userId = 999L;
            when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(UserNotFoundException.class);
            verify(userRepositoryPort).findById(userId);
        }
    }

    @Nested
    @DisplayName("Get User By Email Tests")
    class GetUserByEmailTests {

        @Test
        @DisplayName("Should return user when found by email")
        void getUserByEmail_WhenUserExists_ReturnsUser() {
            // Given
            String email = "test@test.com";
            when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(testUser));

            // When
            User result = userService.getUserByEmail(email);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
            verify(userRepositoryPort).findByEmail(email);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found by email")
        void getUserByEmail_WhenUserNotFound_ThrowsException() {
            // Given
            String email = "nonexistent@test.com";
            when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserByEmail(email))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(email);
            verify(userRepositoryPort).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update user profile successfully")
        void updateProfile_WithValidData_UpdatesUser() {
            // Given
            Long userId = 1L;
            UpdateUserCommand command = new UpdateUserCommand("Jane", "Smith");
            User updatedUser = testUser.toBuilder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepositoryPort.save(any(User.class))).thenReturn(updatedUser);

            // When
            User result = userService.updateProfile(userId, command);

            // Then
            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getLastName()).isEqualTo("Smith");
            verify(userRepositoryPort).findById(userId);
            verify(userRepositoryPort).save(any(User.class));
        }

        @Test
        @DisplayName("Should trim whitespace from names")
        void updateProfile_WithWhitespace_TrimsNames() {
            // Given
            Long userId = 1L;
            UpdateUserCommand command = new UpdateUserCommand("  Jane  ", "  Smith  ");

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            userService.updateProfile(userId, command);

            // Then
            verify(userRepositoryPort).save(any(User.class));
        }

        @Test
        @DisplayName("Should skip null or blank fields")
        void updateProfile_WithNullFields_SkipsNullFields() {
            // Given
            Long userId = 1L;
            UpdateUserCommand command = new UpdateUserCommand(null, "");
            String originalFirstName = testUser.getFirstName();
            String originalLastName = testUser.getLastName();

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(testUser));
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = userService.updateProfile(userId, command);

            // Then
            assertThat(result.getFirstName()).isEqualTo(originalFirstName);
            assertThat(result.getLastName()).isEqualTo(originalLastName);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void updateProfile_WhenUserNotFound_ThrowsException() {
            // Given
            Long userId = 999L;
            UpdateUserCommand command = new UpdateUserCommand("Jane", "Smith");
            when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updateProfile(userId, command))
                    .isInstanceOf(UserNotFoundException.class);
            verify(userRepositoryPort).findById(userId);
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_WithValidCredentials_ChangesPassword() {
            // Given
            Long userId = 1L;
            String currentPassword = "oldPassword123";
            String newPassword = "newPassword123";
            String encodedNewPassword = "encodedNewPassword";

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            userService.changePassword(userId, currentPassword, newPassword);

            // Then
            verify(userRepositoryPort).findById(userId);
            verify(passwordEncoder).matches(anyString(), anyString());
            verify(passwordEncoder).encode(newPassword);
            verify(userRepositoryPort).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when current password is wrong")
        void changePassword_WithWrongCurrentPassword_ThrowsException() {
            // Given
            Long userId = 1L;
            String currentPassword = "wrongPassword";
            String newPassword = "newPassword123";

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(userId, currentPassword, newPassword))
                    .isInstanceOf(InvalidCredentialsException.class);
            verify(userRepositoryPort).findById(userId);
            verify(passwordEncoder).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when new password is too short")
        void changePassword_WithShortPassword_ThrowsException() {
            // Given
            Long userId = 1L;
            String currentPassword = "oldPassword123";
            String newPassword = "123"; // Too short

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(userId, currentPassword, newPassword))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least 6 characters");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when new password is null")
        void changePassword_WithNullPassword_ThrowsException() {
            // Given
            Long userId = 1L;
            String currentPassword = "oldPassword123";
            String newPassword = null;

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(userId, currentPassword, newPassword))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void changePassword_WhenUserNotFound_ThrowsException() {
            // Given
            Long userId = 999L;
            String currentPassword = "oldPassword123";
            String newPassword = "newPassword123";

            when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(userId, currentPassword, newPassword))
                    .isInstanceOf(UserNotFoundException.class);
            verify(userRepositoryPort).findById(userId);
        }
    }
}
