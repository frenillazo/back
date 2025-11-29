package acainfo.back.user.application.services;

import acainfo.back.user.application.ports.in.AuthUseCase;
import acainfo.back.user.domain.exception.InvalidCredentialsException;
import acainfo.back.user.domain.exception.UnauthorizedException;
import acainfo.back.user.domain.exception.UserAlreadyExistsException;
import acainfo.back.user.domain.model.AuditAction;
import acainfo.back.user.domain.model.RefreshToken;
import acainfo.back.user.domain.model.Role;
import acainfo.back.user.domain.model.User;
import acainfo.back.user.domain.model.UserStatus;
import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.infrastructure.adapters.in.dto.AuthResponse;
import acainfo.back.user.infrastructure.adapters.in.dto.LoginRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.RegisterRequest;
import acainfo.back.user.infrastructure.adapters.out.RoleRepository;
import acainfo.back.user.infrastructure.adapters.out.UserRepository;
import acainfo.back.user.infrastructure.security.CustomUserDetails;
import acainfo.back.config.properties.JwtProperties;
import acainfo.back.user.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements AuthUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;
    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            auditService.logFailedLogin(request.getEmail(), "Email already exists");
            throw new UserAlreadyExistsException(request.getEmail());
        }

        // Get STUDENT role by default
        Role studentRole = roleRepository.findByType(RoleType.STUDENT)
                .orElseThrow(() -> new RuntimeException("Student role not found"));

        // Create user
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .build();

        user.addRole(studentRole);
        user = userRepository.save(user);

        // Log audit
        auditService.log(user, AuditAction.USER_CREATED, "User registered");

        // Generate tokens
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User registered successfully: {}", user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    /**
     * Login user
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting to login user: {}", request.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Check if user is active
            if (user.getStatus() != UserStatus.ACTIVE) {
                auditService.logFailedLogin(request.getEmail(), "User account is not active: " + user.getStatus());
                throw new UnauthorizedException("User account is " + user.getStatus());
            }

            // Generate tokens
            String accessToken = jwtService.generateToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            // Log audit
            auditService.log(user, AuditAction.LOGIN, "User logged in");

            log.info("User logged in successfully: {}", user.getEmail());

            return buildAuthResponse(user, accessToken, refreshToken.getToken());

        } catch (BadCredentialsException e) {
            auditService.logFailedLogin(request.getEmail(), "Invalid credentials");
            throw new InvalidCredentialsException();
        }
    }

    /**
     * Refresh access token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        log.info("Attempting to refresh token");

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Generate new access token
        String accessToken = jwtService.generateToken(userDetails);

        log.info("Token refreshed successfully for user: {}", user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    /**
     * Logout user
     */
    @Transactional
    public void logout(String refreshToken) {
        log.info("Attempting to logout user");

        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                User user = userDetails.getUser();

                // Revoke refresh token if provided
                if (refreshToken != null && !refreshToken.isBlank()) {
                    refreshTokenService.revokeToken(refreshToken);
                }

                // Log audit
                auditService.log(user, AuditAction.LOGOUT, "User logged out");

                log.info("User logged out successfully: {}", user.getEmail());
            }
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
        }
    }

    /**
     * Revoke all tokens for current user
     */
    @Transactional
    public void revokeAllTokens() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            refreshTokenService.revokeAllUserTokens(user);
            log.info("All tokens revoked for user: {}", user.getEmail());
        }
    }

    /**
     * Build AuthResponse DTO
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpiration() / 1000) // Convert to seconds
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phone(user.getPhone())
                        .status(user.getStatus().name())
                        .roles(user.getRoles().stream()
                                .map(role -> role.getType().name())
                                .collect(Collectors.toSet()))
                        .build())
                .build();
    }
}
