package com.acainfo.shared.e2e;

import com.acainfo.user.application.dto.AuthenticationResult;
import com.acainfo.user.application.dto.CreateTeacherCommand;
import com.acainfo.user.application.dto.RegisterUserCommand;
import com.acainfo.user.application.port.in.AuthenticateUserUseCase;
import com.acainfo.user.application.port.in.ManageTeachersUseCase;
import com.acainfo.user.application.port.in.RegisterUserUseCase;
import com.acainfo.user.application.dto.AuthenticationCommand;
import com.acainfo.user.domain.model.User;
import org.springframework.stereotype.Component;

/**
 * Helper class for authentication operations in E2E tests.
 * Provides methods to create users and obtain JWT tokens for testing.
 */
@Component
public class TestAuthHelper {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final ManageTeachersUseCase manageTeachersUseCase;

    // Default test credentials
    public static final String DEFAULT_PASSWORD = "Test123!";

    // Admin credentials (must be created via data.sql or bootstrap)
    public static final String ADMIN_EMAIL = "admin@acainfo.com";
    public static final String ADMIN_PASSWORD = "Admin123!";

    public TestAuthHelper(
            RegisterUserUseCase registerUserUseCase,
            AuthenticateUserUseCase authenticateUserUseCase,
            ManageTeachersUseCase manageTeachersUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.manageTeachersUseCase = manageTeachersUseCase;
    }

    // ===========================================
    // Token Retrieval Methods
    // ===========================================

    /**
     * Register a new user and return the access token.
     */
    public String registerAndGetToken(String email, String password, String firstName, String lastName) {
        RegisterUserCommand command = new RegisterUserCommand(email, password, firstName, lastName);
        registerUserUseCase.register(command);
        return login(email, password);
    }

    /**
     * Register a new user with default password and return the access token.
     */
    public String registerAndGetToken(String email, String firstName, String lastName) {
        return registerAndGetToken(email, DEFAULT_PASSWORD, firstName, lastName);
    }

    /**
     * Login with credentials and return the access token.
     */
    public String login(String email, String password) {
        AuthenticationCommand command = new AuthenticationCommand(email, password);
        AuthenticationResult result = authenticateUserUseCase.authenticate(command);
        return result.accessToken();
    }

    /**
     * Get token for a student user.
     * Creates a new student if needed.
     */
    public String getStudentToken() {
        String email = "student-" + System.currentTimeMillis() + "@test.com";
        return registerAndGetToken(email, "Test", "Student");
    }

    /**
     * Get token for a student user with specific email.
     * Creates a new student if needed.
     */
    public String getStudentToken(String email) {
        return registerAndGetToken(email, "Test", "Student");
    }

    /**
     * Get token for a teacher user.
     * Requires admin token to create teacher first.
     */
    public String getTeacherToken(String adminToken) {
        String email = "teacher-" + System.currentTimeMillis() + "@test.com";
        createTeacher(adminToken, email, "Test", "Teacher");
        return login(email, DEFAULT_PASSWORD);
    }

    /**
     * Get token for a teacher user with specific email.
     * Requires admin token to create teacher first.
     */
    public String getTeacherToken(String adminToken, String email) {
        createTeacher(adminToken, email, "Test", "Teacher");
        return login(email, DEFAULT_PASSWORD);
    }

    /**
     * Get token for the admin user.
     * Assumes admin exists in database (via data.sql or bootstrap).
     */
    public String getAdminToken() {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    // ===========================================
    // User Creation Methods
    // ===========================================

    /**
     * Register a new student user.
     */
    public User registerStudent(String email, String password, String firstName, String lastName) {
        RegisterUserCommand command = new RegisterUserCommand(email, password, firstName, lastName);
        return registerUserUseCase.register(command);
    }

    /**
     * Register a new student user with default password.
     */
    public User registerStudent(String email, String firstName, String lastName) {
        return registerStudent(email, DEFAULT_PASSWORD, firstName, lastName);
    }

    /**
     * Create a new teacher (requires admin privileges).
     */
    public User createTeacher(String adminToken, String email, String firstName, String lastName) {
        CreateTeacherCommand command = new CreateTeacherCommand(email, DEFAULT_PASSWORD, firstName, lastName);
        return manageTeachersUseCase.createTeacher(command);
    }

    // ===========================================
    // Utility Methods
    // ===========================================

    /**
     * Create Bearer authorization header value.
     */
    public String bearerToken(String token) {
        return "Bearer " + token;
    }

    /**
     * Generate unique email for testing.
     */
    public String uniqueEmail(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "@test.com";
    }

    /**
     * Generate unique student email.
     */
    public String uniqueStudentEmail() {
        return uniqueEmail("student");
    }

    /**
     * Generate unique teacher email.
     */
    public String uniqueTeacherEmail() {
        return uniqueEmail("teacher");
    }
}
