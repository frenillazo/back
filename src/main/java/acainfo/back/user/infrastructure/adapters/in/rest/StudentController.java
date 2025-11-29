package acainfo.back.user.infrastructure.adapters.in.rest;

import acainfo.back.user.application.services.student.StudentService;
import acainfo.back.user.infrastructure.adapters.in.dto.StudentDashboardResponse;
import acainfo.back.user.infrastructure.adapters.in.dto.StudentProfileResponse;
import acainfo.back.user.infrastructure.adapters.in.dto.UpdateStudentProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import acainfo.back.user.domain.model.User;
import acainfo.back.user.infrastructure.adapters.out.UserRepository;

/**
 * REST Controller for student-specific operations.
 * Provides consolidated endpoints for student dashboard and profile management.
 *
 * This controller focuses on convenience endpoints that aggregate data from multiple modules,
 * providing a better frontend experience by reducing the number of HTTP calls needed.
 *
 * Security: All endpoints require STUDENT role. Students can only access their own data.
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Students", description = "Student dashboard and profile endpoints")
public class StudentController {

    private final StudentService studentService;
    private final UserRepository userRepository;

    // ==================== DASHBOARD ====================

    @GetMapping("/me/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
        summary = "Get student dashboard",
        description = "Returns consolidated dashboard with all student information: profile, enrollments, " +
                      "payments, upcoming sessions, attendance summary, and alerts. " +
                      "This is the main endpoint for the student dashboard UI."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not a student or accessing other student's dashboard")
    })
    public ResponseEntity<StudentDashboardResponse> getMyDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /api/students/me/dashboard - User: {}", userDetails.getUsername());

        User currentUser = getCurrentUser(userDetails);
        StudentDashboardResponse dashboard = studentService.getDashboard(currentUser.getId());

        return ResponseEntity.ok(dashboard);
    }

    // ==================== PROFILE ====================

    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
        summary = "Get my profile",
        description = "Returns the profile information of the authenticated student"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not a student")
    })
    public ResponseEntity<StudentProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("GET /api/students/me/profile - User: {}", userDetails.getUsername());

        User currentUser = getCurrentUser(userDetails);
        StudentProfileResponse profile = studentService.getProfile(currentUser.getId());

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
        summary = "Update my profile",
        description = "Updates the profile information of the authenticated student. " +
                      "Only firstName, lastName, and phone can be updated."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not a student")
    })
    public ResponseEntity<StudentProfileResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateStudentProfileRequest request) {

        log.info("PUT /api/students/me/profile - User: {}", userDetails.getUsername());

        User currentUser = getCurrentUser(userDetails);
        StudentProfileResponse profile = studentService.updateProfile(currentUser.getId(), request);

        log.info("Profile updated successfully for user {}", currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get the current authenticated user entity.
     */
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername());
    }
}
