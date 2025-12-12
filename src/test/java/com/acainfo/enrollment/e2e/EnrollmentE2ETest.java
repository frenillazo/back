package com.acainfo.enrollment.e2e;

import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.ChangeGroupRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.EnrollStudentRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.EnrollmentResponse;
import com.acainfo.group.domain.model.GroupType;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.shared.e2e.BaseE2ETest;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E Integration tests for Enrollment module.
 * Tests CRUD operations for enrollments including waiting list flow.
 */
@DisplayName("Enrollment E2E Tests")
class EnrollmentE2ETest extends BaseE2ETest {

    private static final String BASE_URL = "/api/enrollments";

    private String adminToken;
    private String studentToken;
    private User testStudent;
    private Subject testSubject;
    private SubjectGroup testGroup;

    @BeforeEach
    void setUp() {
        adminToken = authHelper.getAdminToken();
        // Create student
        testStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Test", "Student");
        studentToken = authHelper.login(testStudent.getEmail(), authHelper.DEFAULT_PASSWORD);
        // Create subject + teacher + group
        testSubject = dataHelper.createDefaultSubject();
        User teacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Enrollment", "Teacher");
        testGroup = dataHelper.createGroup(testSubject.getId(), teacher.getId(), GroupType.REGULAR_Q1);
    }

    // ===========================================
    // Create Enrollment Tests
    // ===========================================

    @Nested
    @DisplayName("POST /api/enrollments")
    class CreateEnrollmentTests {

        @Test
        @DisplayName("Should enroll student as ACTIVE when group has capacity")
        void enroll_WhenGroupHasCapacity_ReturnsActiveEnrollment() throws Exception {
            // Given
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();

            // When & Then
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.studentId").value(testStudent.getId()))
                    .andExpect(jsonPath("$.groupId").value(testGroup.getId()))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.waitingListPosition").isEmpty())
                    .andExpect(jsonPath("$.isActive").value(true))
                    .andExpect(jsonPath("$.isOnWaitingList").value(false))
                    .andExpect(jsonPath("$.canBeWithdrawn").value(true));
        }

        @Test
        @DisplayName("Should enroll student as admin")
        void enroll_AsAdmin_ReturnsCreated() throws Exception {
            // Given
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should reject enrollment when student already enrolled")
        void enroll_WhenAlreadyEnrolled_ReturnsConflict() throws Exception {
            // Given - Enroll first
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated());

            // When & Then - Try again
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should reject enrollment with null student ID")
        void enroll_WithNullStudentId_ReturnsBadRequest() throws Exception {
            // Given
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(null)
                    .groupId(testGroup.getId())
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject enrollment with null group ID")
        void enroll_WithNullGroupId_ReturnsBadRequest() throws Exception {
            // Given
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(null)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject enrollment for non-existent group")
        void enroll_WhenGroupNotFound_ReturnsNotFound() throws Exception {
            // Given
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(99999L)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject unauthenticated enrollment request")
        void enroll_WithoutToken_ReturnsForbidden() throws Exception {
            // Given
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();

            // When & Then
            performPost(BASE_URL, request)
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // Get Enrollment Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/enrollments/{id}")
    class GetEnrollmentByIdTests {

        @Test
        @DisplayName("Should get enrollment by ID")
        void getEnrollmentById_WhenExists_ReturnsEnrollment() throws Exception {
            // Given - Create enrollment first
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            MvcResult createResult = performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            EnrollmentResponse createdEnrollment = fromResponse(createResult, EnrollmentResponse.class);

            // When & Then
            performGet(BASE_URL + "/" + createdEnrollment.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdEnrollment.getId()))
                    .andExpect(jsonPath("$.studentId").value(testStudent.getId()))
                    .andExpect(jsonPath("$.groupId").value(testGroup.getId()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent enrollment")
        void getEnrollmentById_WhenNotFound_ReturnsNotFound() throws Exception {
            // When & Then
            performGet(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void getEnrollmentById_WithoutToken_ReturnsForbidden() throws Exception {
            // When & Then
            performGet(BASE_URL + "/1")
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // List Enrollments Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/enrollments")
    class ListEnrollmentsTests {

        @Test
        @DisplayName("Should list enrollments with pagination")
        void getEnrollments_WithPagination_ReturnsPagedResults() throws Exception {
            // Given - Create enrollment
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?page=0&size=10", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").exists())
                    .andExpect(jsonPath("$.number").exists())
                    .andExpect(jsonPath("$.size").exists());
        }

        @Test
        @DisplayName("Should filter enrollments by student ID")
        void getEnrollments_WithStudentIdFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create enrollment
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?studentId=" + testStudent.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter enrollments by group ID")
        void getEnrollments_WithGroupIdFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create enrollment
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?groupId=" + testGroup.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter enrollments by status")
        void getEnrollments_WithStatusFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create enrollment
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?status=ACTIVE", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ===========================================
    // Get Enrollments by Student Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/enrollments/student/{studentId}")
    class GetEnrollmentsByStudentTests {

        @Test
        @DisplayName("Should get active enrollments for student")
        void getActiveEnrollmentsByStudent_ReturnsEnrollmentList() throws Exception {
            // Given - Create enrollment
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "/student/" + testStudent.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Should return empty list when no active enrollments")
        void getActiveEnrollmentsByStudent_WhenNoEnrollments_ReturnsEmptyList() throws Exception {
            // When & Then
            performGet(BASE_URL + "/student/" + testStudent.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ===========================================
    // Get Enrollments by Group Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/enrollments/group/{groupId}")
    class GetEnrollmentsByGroupTests {

        @Test
        @DisplayName("Should get active enrollments for group")
        void getActiveEnrollmentsByGroup_ReturnsEnrollmentList() throws Exception {
            // Given - Create enrollment
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "/group/" + testGroup.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Should return empty list when no enrollments in group")
        void getActiveEnrollmentsByGroup_WhenNoEnrollments_ReturnsEmptyList() throws Exception {
            // When & Then
            performGet(BASE_URL + "/group/" + testGroup.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ===========================================
    // Withdraw Enrollment Tests
    // ===========================================

    @Nested
    @DisplayName("DELETE /api/enrollments/{id}")
    class WithdrawEnrollmentTests {

        @Test
        @DisplayName("Should withdraw active enrollment")
        void withdraw_WhenActive_ReturnsWithdrawnEnrollment() throws Exception {
            // Given - Create enrollment
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            MvcResult createResult = performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            EnrollmentResponse createdEnrollment = fromResponse(createResult, EnrollmentResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdEnrollment.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("WITHDRAWN"))
                    .andExpect(jsonPath("$.isWithdrawn").value(true))
                    .andExpect(jsonPath("$.isActive").value(false))
                    .andExpect(jsonPath("$.withdrawnAt").exists());
        }

        @Test
        @DisplayName("Should withdraw enrollment as admin")
        void withdraw_AsAdmin_ReturnsWithdrawnEnrollment() throws Exception {
            // Given - Create enrollment
            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            MvcResult createResult = performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            EnrollmentResponse createdEnrollment = fromResponse(createResult, EnrollmentResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdEnrollment.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("WITHDRAWN"));
        }

        @Test
        @DisplayName("Should reject withdraw for non-existent enrollment")
        void withdraw_WhenNotFound_ReturnsNotFound() throws Exception {
            // When & Then
            performDelete(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject unauthenticated withdraw request")
        void withdraw_WithoutToken_ReturnsForbidden() throws Exception {
            // When & Then
            performDelete(BASE_URL + "/1")
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // Change Group Tests
    // ===========================================

    @Nested
    @DisplayName("PUT /api/enrollments/{id}/change-group")
    class ChangeGroupTests {

        @Test
        @DisplayName("Should change group when new group has capacity")
        void changeGroup_WhenNewGroupHasCapacity_ReturnsUpdatedEnrollment() throws Exception {
            // Given - Create enrollment in first group
            EnrollStudentRequest enrollRequest = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            MvcResult createResult = performPost(BASE_URL, enrollRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            EnrollmentResponse createdEnrollment = fromResponse(createResult, EnrollmentResponse.class);

            // Create another group with same subject and teacher
            User teacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Another", "Teacher");
            SubjectGroup newGroup = dataHelper.createGroup(testSubject.getId(), teacher.getId(), GroupType.REGULAR_Q2);

            // When
            ChangeGroupRequest changeRequest = ChangeGroupRequest.builder()
                    .newGroupId(newGroup.getId())
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdEnrollment.getId() + "/change-group", changeRequest, studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.groupId").value(newGroup.getId()))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should reject change group for non-existent enrollment")
        void changeGroup_WhenEnrollmentNotFound_ReturnsNotFound() throws Exception {
            // Given
            ChangeGroupRequest changeRequest = ChangeGroupRequest.builder()
                    .newGroupId(testGroup.getId())
                    .build();

            // When & Then
            performPut(BASE_URL + "/99999/change-group", changeRequest, adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject change group for non-existent new group")
        void changeGroup_WhenNewGroupNotFound_ReturnsNotFound() throws Exception {
            // Given - Create enrollment
            EnrollStudentRequest enrollRequest = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            MvcResult createResult = performPost(BASE_URL, enrollRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            EnrollmentResponse createdEnrollment = fromResponse(createResult, EnrollmentResponse.class);

            ChangeGroupRequest changeRequest = ChangeGroupRequest.builder()
                    .newGroupId(99999L)
                    .build();

            // When & Then
            performPut(BASE_URL + "/" + createdEnrollment.getId() + "/change-group", changeRequest, studentToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject change group with null new group ID")
        void changeGroup_WithNullNewGroupId_ReturnsBadRequest() throws Exception {
            // Given - Create enrollment
            EnrollStudentRequest enrollRequest = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(testGroup.getId())
                    .build();
            MvcResult createResult = performPost(BASE_URL, enrollRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            EnrollmentResponse createdEnrollment = fromResponse(createResult, EnrollmentResponse.class);

            ChangeGroupRequest changeRequest = ChangeGroupRequest.builder()
                    .newGroupId(null)
                    .build();

            // When & Then
            performPut(BASE_URL + "/" + createdEnrollment.getId() + "/change-group", changeRequest, studentToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject unauthenticated change group request")
        void changeGroup_WithoutToken_ReturnsForbidden() throws Exception {
            // Given
            ChangeGroupRequest changeRequest = ChangeGroupRequest.builder()
                    .newGroupId(testGroup.getId())
                    .build();

            // When & Then
            performPut(BASE_URL + "/1/change-group", changeRequest)
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // Waiting List Flow Tests
    // ===========================================

    @Nested
    @DisplayName("Waiting List Flow")
    class WaitingListFlowTests {

        @Test
        @DisplayName("Should add to waiting list when group is full")
        void enroll_WhenGroupFull_AddsToWaitingList() throws Exception {
            // Given - Create a group with capacity 1 for easier testing
            User teacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Full", "Teacher");
            SubjectGroup smallGroup = dataHelper.createGroup(testSubject.getId(), teacher.getId(), GroupType.REGULAR_Q1, 1);

            // Enroll first student (fills the group)
            User firstStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "First", "Student");
            EnrollStudentRequest firstRequest = EnrollStudentRequest.builder()
                    .studentId(firstStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            performPost(BASE_URL, firstRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            // When - Enroll second student (should go to waiting list)
            EnrollStudentRequest secondRequest = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();

            // Then
            performPost(BASE_URL, secondRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("WAITING_LIST"))
                    .andExpect(jsonPath("$.waitingListPosition").value(1))
                    .andExpect(jsonPath("$.isOnWaitingList").value(true))
                    .andExpect(jsonPath("$.isActive").value(false));
        }

        @Test
        @DisplayName("Should promote from waiting list when active enrollment withdrawn")
        void withdraw_WhenActiveWithdraws_PromotesFromWaitingList() throws Exception {
            // Given - Create a group with capacity 1
            User teacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Promo", "Teacher");
            SubjectGroup smallGroup = dataHelper.createGroup(testSubject.getId(), teacher.getId(), GroupType.REGULAR_Q1, 1);

            // Enroll first student (fills the group)
            User firstStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "First", "Student");
            EnrollStudentRequest firstRequest = EnrollStudentRequest.builder()
                    .studentId(firstStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            MvcResult firstResult = performPost(BASE_URL, firstRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();
            EnrollmentResponse firstEnrollment = fromResponse(firstResult, EnrollmentResponse.class);

            // Enroll second student (goes to waiting list)
            EnrollStudentRequest secondRequest = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            MvcResult secondResult = performPost(BASE_URL, secondRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("WAITING_LIST"))
                    .andReturn();
            EnrollmentResponse secondEnrollment = fromResponse(secondResult, EnrollmentResponse.class);

            // When - First student withdraws
            performDelete(BASE_URL + "/" + firstEnrollment.getId(), adminToken)
                    .andExpect(status().isOk());

            // Then - Second student should be promoted
            performGet(BASE_URL + "/" + secondEnrollment.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.waitingListPosition").isEmpty())
                    .andExpect(jsonPath("$.promotedAt").exists())
                    .andExpect(jsonPath("$.wasPromotedFromWaitingList").value(true));
        }
    }
}
