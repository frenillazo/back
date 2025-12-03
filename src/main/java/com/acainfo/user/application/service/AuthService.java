package com.acainfo.user.application.service;

import com.acainfo.security.jwt.JwtTokenProvider;
import com.acainfo.security.refresh.RefreshToken;
import com.acainfo.security.refresh.RefreshTokenService;
import com.acainfo.security.userdetails.CustomUserDetails;
import com.acainfo.user.application.dto.AuthenticationCommand;
import com.acainfo.user.application.dto.AuthenticationResult;
import com.acainfo.user.application.dto.RegisterUserCommand;
import com.acainfo.user.application.port.in.AuthenticateUserUseCase;
import com.acainfo.user.application.port.in.LogoutUseCase;
import com.acainfo.user.application.port.in.RefreshTokenUseCase;
import com.acainfo.user.application.port.in.RegisterUserUseCase;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service implementing authentication use cases.
 * Handles user registration, login, token refresh, and logout.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements
        RegisterUserUseCase,
        AuthenticateUserUseCase,
        RefreshTokenUseCase,
        LogoutUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public User register(RegisterUserCommand command) {
        log.info("Registering new user: {}", command.email());

        // Validate email format
        if (!isValidEmail(command.email())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check if email already exists
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new DuplicateEmailException(command.email());
        }

        // Validate password length
        if (command.password() == null || command.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Get STUDENT role
        Role studentRole = roleRepositoryPort.findByType(RoleType.STUDENT)
                .orElseThrow(() -> new IllegalStateException("STUDENT role not found"));

        // Create user
        User user = User.builder()
                .email(command.email().toLowerCase().trim())
                .password(passwordEncoder.encode(command.password()))
                .firstName(command.firstName().trim())
                .lastName(command.lastName().trim())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(studentRole))
                .build();

        User savedUser = userRepositoryPort.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResult authenticate(AuthenticationCommand command) {
        log.info("Authenticating user: {}", command.email());

        try {
            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            command.email(),
                            command.password()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Check if user is active
            if (user.isBlocked()) {
                throw new UserBlockedException(user.getEmail());
            }
            if (!user.isActive()) {
                throw new UserNotActiveException(user.getEmail());
            }

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = refreshTokenService.createRefreshToken(user.getId());

            log.info("User authenticated successfully: {}", user.getEmail());

            return new AuthenticationResult(accessToken, refreshToken, user);

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", command.email());
            throw new InvalidCredentialsException();
        }
    }

    @Override
    @Transactional
    public AuthenticationResult refreshToken(String refreshTokenString) {
        log.info("Refreshing access token");

        // Validate refresh token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenString);

        // Load user
        User user = userRepositoryPort.findById(refreshToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException(refreshToken.getUserId()));

        // Check if user is active
        if (user.isBlocked()) {
            throw new UserBlockedException(user.getEmail());
        }
        if (!user.isActive()) {
            throw new UserNotActiveException(user.getEmail());
        }

        // Generate new tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        // Revoke old refresh token
        refreshTokenService.revokeRefreshToken(refreshTokenString);

        log.info("Access token refreshed for user: {}", user.getEmail());

        return new AuthenticationResult(newAccessToken, newRefreshToken, user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logging out user");
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    @Transactional
    public void logoutAllDevices(Long userId) {
        log.info("Logging out user from all devices: {}", userId);
        refreshTokenService.revokeAllUserTokens(userId);
    }

    /**
     * Validate email format.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
