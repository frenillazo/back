package acainfo.back.user.application.ports.in;

/**
 * Use case port for user authentication operations.
 * Defines the contract for authentication business logic.
 */
public interface AuthenticateUserUseCase {

    /**
     * Command object for user registration.
     */
    record RegisterCommand(
            String email,
            String password,
            String firstName,
            String lastName,
            String phone
    ) {}

    /**
     * Command object for user login.
     */
    record LoginCommand(
            String email,
            String password
    ) {}

    /**
     * Response object for authentication operations.
     */
    record AuthResponse(
            Long userId,
            String email,
            String firstName,
            String lastName,
            String accessToken,
            String refreshToken,
            Long expiresIn,
            java.util.Set<String> roles
    ) {}

    /**
     * Registers a new user in the system.
     *
     * @param command registration data
     * @return authentication response with tokens
     */
    AuthResponse register(RegisterCommand command);

    /**
     * Authenticates a user with email and password.
     *
     * @param command login credentials
     * @return authentication response with tokens
     */
    AuthResponse login(LoginCommand command);

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken the refresh token
     * @return authentication response with new access token
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Logs out a user by revoking their refresh tokens.
     *
     * @param userId the user ID
     */
    void logout(Long userId);

    /**
     * Gets the currently authenticated user ID.
     *
     * @return the current user ID
     */
    Long getCurrentUserId();
}
