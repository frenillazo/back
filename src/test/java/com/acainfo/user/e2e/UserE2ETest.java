package com.acainfo.user.e2e;

import com.acainfo.shared.e2e.BaseE2ETest;
import com.acainfo.shared.e2e.TestAuthHelper;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E Integration tests for User module.
 * Tests authentication, profile management, and teacher CRUD operations.
 */
@DisplayName("User E2E Tests")
class UserE2ETest extends BaseE2ETest {

    // ===========================================
    // Authentication Tests
    // ===========================================

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void register_WithValidData_ReturnsCreated() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest(
                    "newuser@test.com",
                    "Password123!",
                    "John",
                    "Doe"
            );

            // When & Then
            performPost("/api/auth/register", request)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.email").value("newuser@test.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.fullName").value("John Doe"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.roles").isArray())
                    .andExpect(jsonPath("$.roles[0]").value("STUDENT"));
        }

        @Test
        @DisplayName("Should reject duplicate email")
        void register_WithDuplicateEmail_ReturnsConflict() throws Exception {
            // Given - Register first user
            RegisterRequest request1 = new RegisterRequest(
                    "duplicate@test.com",
                    "Password123!",
                    "John",
                    "Doe"
            );
            performPost("/api/auth/register", request1)
                    .andExpect(status().isCreated());

            // When - Try to register with same email
            RegisterRequest request2 = new RegisterRequest(
                    "duplicate@test.com",
                    "Password456!",
                    "Jane",
                    "Smith"
            );

            // Then - DuplicateEmailException returns 400 (Bad Request)
            performPost("/api/auth/register", request2)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject invalid email format")
        void register_WithInvalidEmail_ReturnsBadRequest() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest(
                    "invalid-email",
                    "Password123!",
                    "John",
                    "Doe"
            );

            // When & Then
            performPost("/api/auth/register", request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject short password")
        void register_WithShortPassword_ReturnsBadRequest() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest(
                    "test@test.com",
                    "12345", // Less than 6 characters
                    "John",
                    "Doe"
            );

            // When & Then
            performPost("/api/auth/register", request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject blank first name")
        void register_WithBlankFirstName_ReturnsBadRequest() throws Exception {
            // Given
            RegisterRequest request = new RegisterRequest(
                    "test@test.com",
                    "Password123!",
                    "",
                    "Doe"
            );

            // When & Then
            performPost("/api/auth/register", request)
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_WithValidCredentials_ReturnsTokens() throws Exception {
            // Given - Register user first
            String email = "logintest@test.com";
            String password = "Password123!";
            authHelper.registerStudent(email, password, "Login", "Test");

            LoginRequest request = new LoginRequest(email, password);

            // When & Then
            performPost("/api/auth/login", request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").exists())
                    .andExpect(jsonPath("$.user.email").value(email));
        }

        @Test
        @DisplayName("Should reject invalid password")
        void login_WithInvalidPassword_ReturnsUnauthorized() throws Exception {
            // Given - Register user first
            String email = "wrongpass@test.com";
            authHelper.registerStudent(email, "CorrectPassword", "Test", "User");

            LoginRequest request = new LoginRequest(email, "WrongPassword");

            // When & Then
            performPost("/api/auth/login", request)
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject non-existent user")
        void login_WithNonExistentUser_ReturnsUnauthorized() throws Exception {
            // Given
            LoginRequest request = new LoginRequest("nonexistent@test.com", "Password123!");

            // When & Then
            performPost("/api/auth/login", request)
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should login as admin successfully")
        void login_AsAdmin_ReturnsTokensWithAdminRole() throws Exception {
            // Given - Admin exists from data-test.sql
            LoginRequest request = new LoginRequest(
                    TestAuthHelper.ADMIN_EMAIL,
                    TestAuthHelper.ADMIN_PASSWORD
            );

            // When & Then
            performPost("/api/auth/login", request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.user.email").value(TestAuthHelper.ADMIN_EMAIL))
                    .andExpect(jsonPath("$.user.roles[0]").value("ADMIN"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void refresh_WithValidToken_ReturnsNewTokens() throws Exception {
            // Given - Login to get refresh token
            String email = "refreshtest@test.com";
            authHelper.registerStudent(email, TestAuthHelper.DEFAULT_PASSWORD, "Refresh", "Test");

            LoginRequest loginRequest = new LoginRequest(email, TestAuthHelper.DEFAULT_PASSWORD);
            MvcResult loginResult = performPost("/api/auth/login", loginRequest)
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse authResponse = fromResponse(loginResult, AuthResponse.class);
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest(authResponse.refreshToken());

            // When & Then
            performPost("/api/auth/refresh", refreshRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.user.email").value(email));
        }

        @Test
        @DisplayName("Should reject invalid refresh token")
        void refresh_WithInvalidToken_ReturnsUnauthorized() throws Exception {
            // Given
            RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");

            // When & Then
            performPost("/api/auth/refresh", request)
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully")
        void logout_WithValidToken_ReturnsOk() throws Exception {
            // Given - Login to get refresh token
            String email = "logouttest@test.com";
            authHelper.registerStudent(email, TestAuthHelper.DEFAULT_PASSWORD, "Logout", "Test");

            LoginRequest loginRequest = new LoginRequest(email, TestAuthHelper.DEFAULT_PASSWORD);
            MvcResult loginResult = performPost("/api/auth/login", loginRequest)
                    .andExpect(status().isOk())
                    .andReturn();

            AuthResponse authResponse = fromResponse(loginResult, AuthResponse.class);
            RefreshTokenRequest logoutRequest = new RefreshTokenRequest(authResponse.refreshToken());

            // When & Then
            performPost("/api/auth/logout", logoutRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logout successful"));
        }
    }

    // ===========================================
    // Profile Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/users/profile")
    class GetProfileTests {

        @Test
        @DisplayName("Should return profile for authenticated user")
        void getProfile_WithValidToken_ReturnsProfile() throws Exception {
            // Given
            String email = "profiletest@test.com";
            String token = authHelper.registerAndGetToken(email, "Profile", "Test");

            // When & Then
            performGet("/api/users/profile", token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(email))
                    .andExpect(jsonPath("$.firstName").value("Profile"))
                    .andExpect(jsonPath("$.lastName").value("Test"))
                    .andExpect(jsonPath("$.fullName").value("Profile Test"));
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void getProfile_WithoutToken_ReturnsForbidden() throws Exception {
            // When & Then - Spring Security returns 403 for missing token
            performGet("/api/users/profile")
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject invalid token")
        void getProfile_WithInvalidToken_ReturnsForbidden() throws Exception {
            // When & Then - Spring Security returns 403 for invalid token
            performGet("/api/users/profile", "invalid-token")
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/profile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile successfully")
        void updateProfile_WithValidData_ReturnsUpdatedProfile() throws Exception {
            // Given
            String email = "updateprofile@test.com";
            String token = authHelper.registerAndGetToken(email, "Original", "Name");

            UpdateProfileRequest request = new UpdateProfileRequest("Updated", "User");

            // When & Then
            performPut("/api/users/profile", request, token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Updated"))
                    .andExpect(jsonPath("$.lastName").value("User"))
                    .andExpect(jsonPath("$.fullName").value("Updated User"));
        }

        @Test
        @DisplayName("Should reject blank first name")
        void updateProfile_WithBlankFirstName_ReturnsBadRequest() throws Exception {
            // Given
            String token = authHelper.getStudentToken();
            UpdateProfileRequest request = new UpdateProfileRequest("", "User");

            // When & Then
            performPut("/api/users/profile", request, token)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void updateProfile_WithoutToken_ReturnsForbidden() throws Exception {
            // Given
            UpdateProfileRequest request = new UpdateProfileRequest("Updated", "User");

            // When & Then - Spring Security returns 403 for invalid token
            performPut("/api/users/profile", request, "invalid-token")
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // Teacher Management Tests (Admin only)
    // ===========================================

    @Nested
    @DisplayName("POST /api/teachers")
    class CreateTeacherTests {

        @Test
        @DisplayName("Should create teacher as admin")
        void createTeacher_AsAdmin_ReturnsCreated() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            CreateTeacherRequest request = new CreateTeacherRequest(
                    "newteacher@test.com",
                    "Teacher123!",
                    "New",
                    "Teacher"
            );

            // When & Then
            performPost("/api/teachers", request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("newteacher@test.com"))
                    .andExpect(jsonPath("$.firstName").value("New"))
                    .andExpect(jsonPath("$.lastName").value("Teacher"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should reject teacher creation by student")
        void createTeacher_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateTeacherRequest request = new CreateTeacherRequest(
                    "forbidden@test.com",
                    "Teacher123!",
                    "Forbidden",
                    "Teacher"
            );

            // When & Then
            performPost("/api/teachers", request, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject duplicate teacher email")
        void createTeacher_WithDuplicateEmail_ReturnsConflict() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            String email = "duplicate-teacher@test.com";

            CreateTeacherRequest request1 = new CreateTeacherRequest(
                    email, "Teacher123!", "First", "Teacher"
            );
            performPost("/api/teachers", request1, adminToken)
                    .andExpect(status().isCreated());

            // When - Try to create teacher with same email
            CreateTeacherRequest request2 = new CreateTeacherRequest(
                    email, "Teacher456!", "Second", "Teacher"
            );

            // Then - DuplicateEmailException returns 400 (Bad Request)
            performPost("/api/teachers", request2, adminToken)
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/teachers")
    class GetTeachersTests {

        @Test
        @DisplayName("Should list teachers as admin")
        void getTeachers_AsAdmin_ReturnsTeacherList() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // Create a teacher first
            CreateTeacherRequest createRequest = new CreateTeacherRequest(
                    "listteacher@test.com",
                    "Teacher123!",
                    "List",
                    "Teacher"
            );
            performPost("/api/teachers", createRequest, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet("/api/teachers", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").exists())
                    .andExpect(jsonPath("$.page").exists());
        }

        @Test
        @DisplayName("Should reject teacher list request by student")
        void getTeachers_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();

            // When & Then
            performGet("/api/teachers", studentToken)
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/teachers/{id}")
    class GetTeacherByIdTests {

        @Test
        @DisplayName("Should get teacher by ID as admin")
        void getTeacherById_AsAdmin_ReturnsTeacher() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // Create a teacher first
            CreateTeacherRequest createRequest = new CreateTeacherRequest(
                    "getbyid@test.com",
                    "Teacher123!",
                    "GetById",
                    "Teacher"
            );
            MvcResult createResult = performPost("/api/teachers", createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            TeacherResponse createdTeacher = fromResponse(createResult, TeacherResponse.class);

            // When & Then
            performGet("/api/teachers/" + createdTeacher.id(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdTeacher.id()))
                    .andExpect(jsonPath("$.email").value("getbyid@test.com"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent teacher")
        void getTeacherById_WithInvalidId_ReturnsNotFound() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // When & Then
            performGet("/api/teachers/99999", adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/teachers/{id}")
    class UpdateTeacherTests {

        @Test
        @DisplayName("Should update teacher as admin")
        void updateTeacher_AsAdmin_ReturnsUpdatedTeacher() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // Create a teacher first
            CreateTeacherRequest createRequest = new CreateTeacherRequest(
                    "updateteacher@test.com",
                    "Teacher123!",
                    "Original",
                    "Teacher"
            );
            MvcResult createResult = performPost("/api/teachers", createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            TeacherResponse createdTeacher = fromResponse(createResult, TeacherResponse.class);

            // When
            UpdateTeacherRequest updateRequest = new UpdateTeacherRequest("Updated", "Name");

            // Then
            performPut("/api/teachers/" + createdTeacher.id(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Updated"))
                    .andExpect(jsonPath("$.lastName").value("Name"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/teachers/{id}")
    class DeleteTeacherTests {

        @Test
        @DisplayName("Should delete teacher as admin")
        void deleteTeacher_AsAdmin_ReturnsOk() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // Create a teacher first
            CreateTeacherRequest createRequest = new CreateTeacherRequest(
                    "deleteteacher@test.com",
                    "Teacher123!",
                    "Delete",
                    "Teacher"
            );
            MvcResult createResult = performPost("/api/teachers", createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            TeacherResponse createdTeacher = fromResponse(createResult, TeacherResponse.class);

            // When & Then
            performDelete("/api/teachers/" + createdTeacher.id(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Teacher deleted successfully"));

            // Verify teacher is  accessible (should return 200) but status is blocked
            performGet("/api/teachers/" + createdTeacher.id(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("BLOCKED"));
        }

        @Test
        @DisplayName("Should reject delete by student")
        void deleteTeacher_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            String studentToken = authHelper.getStudentToken();

            // Create a teacher first
            CreateTeacherRequest createRequest = new CreateTeacherRequest(
                    "nodelete@test.com",
                    "Teacher123!",
                    "NoDelete",
                    "Teacher"
            );
            MvcResult createResult = performPost("/api/teachers", createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            TeacherResponse createdTeacher = fromResponse(createResult, TeacherResponse.class);

            // When & Then
            performDelete("/api/teachers/" + createdTeacher.id(), studentToken)
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // Admin User Management Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/admin/users")
    class AdminGetUsersTests {

        @Test
        @DisplayName("Should list all users as admin")
        void getUsers_AsAdmin_ReturnsUserList() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // When & Then
            performGet("/api/admin/users", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").exists());
        }

        @Test
        @DisplayName("Should reject user list request by student")
        void getUsers_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();

            // When & Then
            performGet("/api/admin/users", studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should filter users by status")
        void getUsers_WithStatusFilter_ReturnsFilteredList() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // When & Then
            performGet("/api/admin/users?status=ACTIVE", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }
}
