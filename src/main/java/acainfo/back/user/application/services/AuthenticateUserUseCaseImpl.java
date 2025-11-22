package acainfo.back.user.application.services;

import acainfo.back.user.application.ports.in.AuthenticateUserUseCase;
import acainfo.back.user.application.ports.out.RefreshTokenRepositoryPort;
import acainfo.back.user.application.ports.out.RoleRepositoryPort;
import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.exception.DuplicateEmailException;
import acainfo.back.user.domain.exception.InvalidCredentialsException;
import acainfo.back.user.domain.exception.InvalidTokenException;
import acainfo.back.user.domain.exception.RoleNotFoundException;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.domain.model.RefreshTokenDomain;
import acainfo.back.user.domain.model.RoleDomain;
import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.domain.model.UserDomain;
import acainfo.back.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AuthenticateUserUseCase.
 * Handles user authentication, registration, and token management.
 * <p>
 * TODO: Integrate with JwtService once security config is migrated
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    // TODO: Inject JwtService once it's migrated to user module

    private static final long ACCESS_TOKEN_EXPIRY = 3600L; // 1 hour in seconds
    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7L; // 7 days

    @Override
    @Transactional
    public AuthResponse register(RegisterCommand command) {
        log.debug("Registering new user with email: {}", command.email());

        // Validate email uniqueness
        if (userRepository.existsByEmailIgnoreCase(command.email())) {
            throw new DuplicateEmailException(command.email());
        }

        // Get student role (default for registration)
        RoleDomain studentRole = roleRepository.findByType(RoleType.STUDENT)
                .orElseThrow(() -> new RoleNotFoundException(RoleType.STUDENT));

        // Create user
        Set<RoleDomain> roles = new HashSet<>();
        roles.add(studentRole);

        UserDomain user = UserDomain.builder()
                .email(command.email())
                .password(passwordEncoder.encode(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .phone(command.phone())
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        user.validate();
        UserDomain savedUser = userRepository.save(user);

        log.info("User registered successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        // Generate tokens
        return generateAuthResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginCommand command) {
        log.debug("User login attempt: email={}", command.email());

        // Find user by email
        UserDomain user = userRepository.findByEmailIgnoreCase(command.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            log.warn("Failed login attempt for email: {}", command.email());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Check if user is active
        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is not active");
        }

        log.info("User logged in successfully: id={}, email={}", user.getId(), user.getEmail());

        // Generate tokens
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Refreshing access token");

        // Find refresh token
        RefreshTokenDomain token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // Validate token
        if (!token.isValid()) {
            if (token.isRevoked()) {
                throw new InvalidTokenException("Refresh token has been revoked");
            } else {
                throw new InvalidTokenException("Refresh token has expired");
            }
        }

        UserDomain user = token.getUser();

        log.info("Access token refreshed for user: id={}", user.getId());

        // Generate new auth response
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        log.debug("Logging out user: id={}", userId);

        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new UserNotFoundException(userId);
        }

        // Revoke all refresh tokens for user
        refreshTokenRepository.revokeAllByUserId(userId);

        log.info("User logged out successfully: id={}", userId);
    }

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // TODO: Extract user ID from authentication principal once JWT integration is complete
        // For now, try to parse from name (assuming it's the email)
        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCase(email)
                .map(UserDomain::getId)
                .orElse(null);
    }

    /**
     * Generates authentication response with access and refresh tokens.
     *
     * @param user the authenticated user
     * @return authentication response
     */
    private AuthResponse generateAuthResponse(UserDomain user) {
        // TODO: Use JwtService to generate actual JWT tokens once it's migrated
        // For now, generate placeholder tokens
        String accessToken = "jwt_access_token_" + UUID.randomUUID();
        String refreshToken = createRefreshToken(user);

        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getType().name())
                .collect(Collectors.toSet());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                accessToken,
                refreshToken,
                ACCESS_TOKEN_EXPIRY,
                roleNames
        );
    }

    /**
     * Creates and persists a new refresh token for the user.
     *
     * @param user the user
     * @return the refresh token string
     */
    private String createRefreshToken(UserDomain user) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS);

        RefreshTokenDomain refreshToken = RefreshTokenDomain.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }
}
