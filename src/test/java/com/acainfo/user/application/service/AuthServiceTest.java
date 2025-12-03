package com.acainfo.user.application.service;

import com.acainfo.security.jwt.JwtTokenProvider;
import com.acainfo.security.refresh.RefreshToken;
import com.acainfo.security.refresh.RefreshTokenService;
import com.acainfo.security.userdetails.CustomUserDetails;
import com.acainfo.shared.factory.RoleFactory;
import com.acainfo.shared.factory.UserFactory;
import com.acainfo.user.application.dto.AuthenticationCommand;
import com.acainfo.user.application.dto.AuthenticationResult;
import com.acainfo.user.application.dto.RegisterUserCommand;
import com.acainfo.user.application.port.out.RoleRepositoryPort;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.DuplicateEmailException;
import com.acainfo.user.domain.exception.InvalidCredentialsException;
import com.acainfo.user.domain.exception.UserBlockedException;
import com.acainfo.user.domain.exception.UserNotActiveException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService with Mockito.
 * Tests authentication and registration logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        studentRole = RoleFactory.defaultStudentRole();
        testUser = UserFactory.defaultStudent();
    }

    @Nested
    @DisplayName("Register User Tests")
    class RegisterUserTests {

        @Test
        @DisplayName("Should register user successfully")
        void register_WithValidData_CreatesUser() {
            // Given
            RegisterUserCommand command = new RegisterUserCommand(
                    "newuser@test.com",
                    "password123",
                    "John",
                    "Doe"
            );
            String encodedPassword = "encodedPassword";

            when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
            when(roleRepositoryPort.findByType(RoleType.STUDENT)).thenReturn(Optional.of(studentRole));
            when(passwordEncoder.encode(command.password())).thenReturn(encodedPassword);
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return user.toBuilder().id(1L).build();
            });

            // When
            User result = authService.register(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("newuser@test.com");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(result.isStudent()).isTrue();

            verify(userRepositoryPort).existsByEmail("newuser@test.com");
            verify(roleRepositoryPort).findByType(RoleType.STUDENT);
            verify(passwordEncoder).encode(command.password());
            verify(userRepositoryPort).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw DuplicateEmailException when email exists")
        void register_WithDuplicateEmail_ThrowsException() {
            // Given
            RegisterUserCommand command = new RegisterUserCommand(
                    "existing@test.com",
                    "password123",
                    "John",
                    "Doe"
            );

            when(userRepositoryPort.existsByEmail("existing@test.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(command))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("existing@test.com");

            verify(userRepositoryPort).existsByEmail("existing@test.com");
            verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException with invalid email")
        void register_WithInvalidEmail_ThrowsException() {
            // Given
            RegisterUserCommand command = new RegisterUserCommand(
                    "invalid-email",
                    "password123",
                    "John",
                    "Doe"
            );

            // When & Then
            assertThatThrownBy(() -> authService.register(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email format");

            verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException with short password")
        void register_WithShortPassword_ThrowsException() {
            // Given
            RegisterUserCommand command = new RegisterUserCommand(
                    "test@test.com",
                    "123", // Too short
                    "John",
                    "Doe"
            );

            when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.register(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least 6 characters");

            verify(userRepositoryPort, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should normalize email to lowercase and trim")
        void register_WithMixedCaseEmail_NormalizesEmail() {
            // Given
            RegisterUserCommand command = new RegisterUserCommand(
                    "Test@TEST.COM",
                    "password123",
                    "John",
                    "Doe"
            );

            when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
            when(roleRepositoryPort.findByType(RoleType.STUDENT)).thenReturn(Optional.of(studentRole));
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            User result = authService.register(command);

            // Then
            assertThat(result.getEmail()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when STUDENT role not found")
        void register_WhenStudentRoleNotFound_ThrowsException() {
            // Given
            RegisterUserCommand command = new RegisterUserCommand(
                    "test@test.com",
                    "password123",
                    "John",
                    "Doe"
            );

            when(userRepositoryPort.existsByEmail(anyString())).thenReturn(false);
            when(roleRepositoryPort.findByType(RoleType.STUDENT)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.register(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("STUDENT role not found");
        }
    }

    @Nested
    @DisplayName("Authenticate User Tests")
    class AuthenticateUserTests {

        @Test
        @DisplayName("Should authenticate user successfully")
        void authenticate_WithValidCredentials_ReturnsAuthResult() {
            // Given
            AuthenticationCommand command = new AuthenticationCommand(
                    "student@test.com",
                    "password123"
            );
            CustomUserDetails userDetails = new CustomUserDetails(testUser);
            String accessToken = "accessToken";
            String refreshToken = "refreshToken";

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn(accessToken);
            when(refreshTokenService.createRefreshToken(testUser.getId())).thenReturn(refreshToken);

            // When
            AuthenticationResult result = authService.authenticate(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(accessToken);
            assertThat(result.refreshToken()).isEqualTo(refreshToken);
            assertThat(result.user()).isEqualTo(testUser);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtTokenProvider).generateAccessToken(userDetails);
            verify(refreshTokenService).createRefreshToken(testUser.getId());
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException with wrong password")
        void authenticate_WithInvalidCredentials_ThrowsException() {
            // Given
            AuthenticationCommand command = new AuthenticationCommand(
                    "student@test.com",
                    "wrongPassword"
            );

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.authenticate(command))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtTokenProvider, never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("Should throw UserBlockedException when user is blocked")
        void authenticate_WithBlockedUser_ThrowsException() {
            // Given
            AuthenticationCommand command = new AuthenticationCommand(
                    "student@test.com",
                    "password123"
            );
            User blockedUser = UserFactory.blockedStudent();
            CustomUserDetails userDetails = new CustomUserDetails(blockedUser);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);

            // When & Then
            assertThatThrownBy(() -> authService.authenticate(command))
                    .isInstanceOf(UserBlockedException.class)
                    .hasMessageContaining(blockedUser.getEmail());

            verify(jwtTokenProvider, never()).generateAccessToken(any());
        }

        @Test
        @DisplayName("Should throw UserNotActiveException when user is not active")
        void authenticate_WithInactiveUser_ThrowsException() {
            // Given
            AuthenticationCommand command = new AuthenticationCommand(
                    "student@test.com",
                    "password123"
            );
            User pendingUser = UserFactory.pendingStudent();
            CustomUserDetails userDetails = new CustomUserDetails(pendingUser);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);

            // When & Then
            assertThatThrownBy(() -> authService.authenticate(command))
                    .isInstanceOf(UserNotActiveException.class)
                    .hasMessageContaining(pendingUser.getEmail());

            verify(jwtTokenProvider, never()).generateAccessToken(any());
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void refreshToken_WithValidToken_ReturnsNewTokens() {
            // Given
            String oldRefreshToken = "oldRefreshToken";
            String newAccessToken = "newAccessToken";
            String newRefreshToken = "newRefreshToken";

            RefreshToken refreshToken = RefreshToken.builder()
                    .token(oldRefreshToken)
                    .userId(testUser.getId())
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .revoked(false)
                    .build();

            CustomUserDetails userDetails = new CustomUserDetails(testUser);

            when(refreshTokenService.validateRefreshToken(oldRefreshToken)).thenReturn(refreshToken);
            when(userRepositoryPort.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(jwtTokenProvider.generateAccessToken(any(CustomUserDetails.class))).thenReturn(newAccessToken);
            when(refreshTokenService.createRefreshToken(testUser.getId())).thenReturn(newRefreshToken);

            // When
            AuthenticationResult result = authService.refreshToken(oldRefreshToken);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(newAccessToken);
            assertThat(result.refreshToken()).isEqualTo(newRefreshToken);
            assertThat(result.user()).isEqualTo(testUser);

            verify(refreshTokenService).validateRefreshToken(oldRefreshToken);
            verify(userRepositoryPort).findById(testUser.getId());
            verify(refreshTokenService).revokeRefreshToken(oldRefreshToken);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found")
        void refreshToken_WithNonExistentUser_ThrowsException() {
            // Given
            String refreshTokenString = "refreshToken";

            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenString)
                    .userId(999L)
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .build();

            when(refreshTokenService.validateRefreshToken(refreshTokenString)).thenReturn(refreshToken);
            when(userRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(refreshTokenString))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw UserBlockedException when user is blocked")
        void refreshToken_WithBlockedUser_ThrowsException() {
            // Given
            String refreshTokenString = "refreshToken";
            User blockedUser = UserFactory.blockedStudent();

            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenString)
                    .userId(blockedUser.getId())
                    .expiresAt(LocalDateTime.now().plusDays(1))
                    .build();

            when(refreshTokenService.validateRefreshToken(refreshTokenString)).thenReturn(refreshToken);
            when(userRepositoryPort.findById(blockedUser.getId())).thenReturn(Optional.of(blockedUser));

            // When & Then
            assertThatThrownBy(() -> authService.refreshToken(refreshTokenString))
                    .isInstanceOf(UserBlockedException.class);
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully")
        void logout_WithValidToken_RevokesToken() {
            // Given
            String refreshToken = "refreshToken";

            // When
            authService.logout(refreshToken);

            // Then
            verify(refreshTokenService).revokeRefreshToken(refreshToken);
        }

        @Test
        @DisplayName("Should logout from all devices successfully")
        void logoutAllDevices_RevokesAllUserTokens() {
            // Given
            Long userId = 1L;

            // When
            authService.logoutAllDevices(userId);

            // Then
            verify(refreshTokenService).revokeAllUserTokens(userId);
        }
    }
}
