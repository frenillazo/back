package com.acainfo.enrollment.e2e;

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
 * E2E Integration tests for Waiting Queue operations.
 * Tests FIFO waiting list management including leaving queue and position adjustments.
 */
@DisplayName("Waiting Queue E2E Tests")
class WaitingQueueE2ETest extends BaseE2ETest {

    private static final String BASE_URL = "/api/waiting-queue";
    private static final String ENROLLMENTS_URL = "/api/enrollments";

    private String adminToken;
    private Subject testSubject;
    private SubjectGroup smallGroup; // Group with capacity 1 for waiting list tests

    @BeforeEach
    void setUp() {
        adminToken = authHelper.getAdminToken();
        testSubject = dataHelper.createDefaultSubject();
        // Create group with capacity 1 to easily trigger waiting list
        User teacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Queue", "Teacher");
        smallGroup = dataHelper.createGroup(testSubject.getId(), teacher.getId(), GroupType.REGULAR_Q1, 1);
    }

    // ===========================================
    // Get Waiting List by Group Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/waiting-queue/group/{groupId}")
    class GetWaitingListByGroupTests {

        @Test
        @DisplayName("Should return waiting list ordered by position (FIFO)")
        void getWaitingListByGroup_ReturnsOrderedList() throws Exception {
            // Given - Fill the group and add students to waiting list
            User activeStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Active", "Student");
            EnrollStudentRequest activeRequest = EnrollStudentRequest.builder()
                    .studentId(activeStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            performPost(ENROLLMENTS_URL, activeRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            // Add first to waiting list
            User waitingStudent1 = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Waiting1", "Student");
            EnrollStudentRequest waitingRequest1 = EnrollStudentRequest.builder()
                    .studentId(waitingStudent1.getId())
                    .groupId(smallGroup.getId())
                    .build();
            performPost(ENROLLMENTS_URL, waitingRequest1, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("WAITING_LIST"))
                    .andExpect(jsonPath("$.waitingListPosition").value(1));

            // Add second to waiting list
            User waitingStudent2 = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Waiting2", "Student");
            EnrollStudentRequest waitingRequest2 = EnrollStudentRequest.builder()
                    .studentId(waitingStudent2.getId())
                    .groupId(smallGroup.getId())
                    .build();
            performPost(ENROLLMENTS_URL, waitingRequest2, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("WAITING_LIST"))
                    .andExpect(jsonPath("$.waitingListPosition").value(2));

            // When & Then
            performGet(BASE_URL + "/group/" + smallGroup.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].waitingListPosition").value(1))
                    .andExpect(jsonPath("$[1].waitingListPosition").value(2));
        }

        @Test
        @DisplayName("Should return empty list when no students waiting")
        void getWaitingListByGroup_WhenEmpty_ReturnsEmptyList() throws Exception {
            // When & Then
            performGet(BASE_URL + "/group/" + smallGroup.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void getWaitingListByGroup_WithoutToken_ReturnsForbidden() throws Exception {
            // When & Then
            performGet(BASE_URL + "/group/" + smallGroup.getId())
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // Get Waiting List by Student Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/waiting-queue/student/{studentId}")
    class GetWaitingListByStudentTests {

        @Test
        @DisplayName("Should return all waiting positions for student")
        void getWaitingListByStudent_ReturnsAllQueues() throws Exception {
            // Given - Fill the first group
            User activeStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Active", "Student");
            EnrollStudentRequest activeRequest = EnrollStudentRequest.builder()
                    .studentId(activeStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            performPost(ENROLLMENTS_URL, activeRequest, adminToken)
                    .andExpect(status().isCreated());

            // Create test student
            User testStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Test", "Student");
            String studentToken = authHelper.login(testStudent.getEmail(), authHelper.DEFAULT_PASSWORD);

            // Add test student to waiting list of first group
            EnrollStudentRequest waitingRequest1 = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            performPost(ENROLLMENTS_URL, waitingRequest1, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("WAITING_LIST"));

            // Create second full group and add test student to its waiting list
            User teacher2 = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Queue2", "Teacher");
            SubjectGroup smallGroup2 = dataHelper.createGroup(testSubject.getId(), teacher2.getId(), GroupType.REGULAR_Q2, 1);

            // Fill second group
            User activeStudent2 = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Active2", "Student");
            EnrollStudentRequest activeRequest2 = EnrollStudentRequest.builder()
                    .studentId(activeStudent2.getId())
                    .groupId(smallGroup2.getId())
                    .build();
            performPost(ENROLLMENTS_URL, activeRequest2, adminToken)
                    .andExpect(status().isCreated());

            // Add test student to second waiting list
            EnrollStudentRequest waitingRequest2 = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(smallGroup2.getId())
                    .build();
            performPost(ENROLLMENTS_URL, waitingRequest2, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("WAITING_LIST"));

            // When & Then
            performGet(BASE_URL + "/student/" + testStudent.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should return empty list when student not on any waiting list")
        void getWaitingListByStudent_WhenNotWaiting_ReturnsEmptyList() throws Exception {
            // Given
            User testStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Test", "Student");
            String studentToken = authHelper.login(testStudent.getEmail(), authHelper.DEFAULT_PASSWORD);

            // When & Then
            performGet(BASE_URL + "/student/" + testStudent.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ===========================================
    // Leave Waiting List Tests
    // ===========================================

    @Nested
    @DisplayName("DELETE /api/waiting-queue/{id}")
    class LeaveWaitingListTests {

        @Test
        @DisplayName("Should leave waiting list and adjust positions")
        void leaveWaitingList_WithdrawsAndAdjustsPositions() throws Exception {
            // Given - Fill the group
            User activeStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Active", "Student");
            EnrollStudentRequest activeRequest = EnrollStudentRequest.builder()
                    .studentId(activeStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            performPost(ENROLLMENTS_URL, activeRequest, adminToken)
                    .andExpect(status().isCreated());

            // Add first student to waiting list (position 1)
            User waitingStudent1 = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Waiting1", "Student");
            String waiting1Token = authHelper.login(waitingStudent1.getEmail(), authHelper.DEFAULT_PASSWORD);
            EnrollStudentRequest waitingRequest1 = EnrollStudentRequest.builder()
                    .studentId(waitingStudent1.getId())
                    .groupId(smallGroup.getId())
                    .build();
            MvcResult result1 = performPost(ENROLLMENTS_URL, waitingRequest1, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();
            EnrollmentResponse enrollment1 = fromResponse(result1, EnrollmentResponse.class);

            // Add second student to waiting list (position 2)
            User waitingStudent2 = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Waiting2", "Student");
            EnrollStudentRequest waitingRequest2 = EnrollStudentRequest.builder()
                    .studentId(waitingStudent2.getId())
                    .groupId(smallGroup.getId())
                    .build();
            MvcResult result2 = performPost(ENROLLMENTS_URL, waitingRequest2, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();
            EnrollmentResponse enrollment2 = fromResponse(result2, EnrollmentResponse.class);

            // Add third student to waiting list (position 3)
            User waitingStudent3 = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Waiting3", "Student");
            EnrollStudentRequest waitingRequest3 = EnrollStudentRequest.builder()
                    .studentId(waitingStudent3.getId())
                    .groupId(smallGroup.getId())
                    .build();
            MvcResult result3 = performPost(ENROLLMENTS_URL, waitingRequest3, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();
            EnrollmentResponse enrollment3 = fromResponse(result3, EnrollmentResponse.class);

            // When - First waiting student leaves
            performDelete(BASE_URL + "/" + enrollment1.getId(), waiting1Token)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("WITHDRAWN"))
                    .andExpect(jsonPath("$.waitingListPosition").isEmpty());

            // Then - Waiting list should now have 2 students
            performGet(BASE_URL + "/group/" + smallGroup.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should reject leave for active enrollment (use withdraw instead)")
        void leaveWaitingList_WhenActive_ReturnsBadRequest() throws Exception {
            // Given - Create active enrollment
            User testStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Test", "Student");
            String studentToken = authHelper.login(testStudent.getEmail(), authHelper.DEFAULT_PASSWORD);

            EnrollStudentRequest request = EnrollStudentRequest.builder()
                    .studentId(testStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            MvcResult result = performPost(ENROLLMENTS_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andReturn();

            EnrollmentResponse enrollment = fromResponse(result, EnrollmentResponse.class);

            // When & Then - Should reject leave (not on waiting list)
            performDelete(BASE_URL + "/" + enrollment.getId(), studentToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject leave for non-existent enrollment")
        void leaveWaitingList_WhenNotFound_ReturnsNotFound() throws Exception {
            // When & Then
            performDelete(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should allow admin to remove student from waiting list")
        void leaveWaitingList_AsAdmin_ReturnsOk() throws Exception {
            // Given - Fill the group
            User activeStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Active", "Student");
            EnrollStudentRequest activeRequest = EnrollStudentRequest.builder()
                    .studentId(activeStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            performPost(ENROLLMENTS_URL, activeRequest, adminToken)
                    .andExpect(status().isCreated());

            // Add student to waiting list
            User waitingStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Waiting", "Student");
            EnrollStudentRequest waitingRequest = EnrollStudentRequest.builder()
                    .studentId(waitingStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            MvcResult result = performPost(ENROLLMENTS_URL, waitingRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            EnrollmentResponse enrollment = fromResponse(result, EnrollmentResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + enrollment.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("WITHDRAWN"));
        }

        @Test
        @DisplayName("Should reject unauthenticated leave request")
        void leaveWaitingList_WithoutToken_ReturnsForbidden() throws Exception {
            // When & Then
            performDelete(BASE_URL + "/1")
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // FIFO Order Tests
    // ===========================================

    @Nested
    @DisplayName("FIFO Order Tests")
    class FifoOrderTests {

        @Test
        @DisplayName("Should maintain FIFO order after middle student leaves")
        void fifoOrder_WhenMiddleLeaves_MaintainsOrder() throws Exception {
            // Given - Fill the group
            User activeStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Active", "Student");
            EnrollStudentRequest activeRequest = EnrollStudentRequest.builder()
                    .studentId(activeStudent.getId())
                    .groupId(smallGroup.getId())
                    .build();
            performPost(ENROLLMENTS_URL, activeRequest, adminToken)
                    .andExpect(status().isCreated());

            // Add three students to waiting list
            User[] waitingStudents = new User[3];
            EnrollmentResponse[] enrollments = new EnrollmentResponse[3];

            for (int i = 0; i < 3; i++) {
                waitingStudents[i] = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Waiting" + (i + 1), "Student");
                EnrollStudentRequest request = EnrollStudentRequest.builder()
                        .studentId(waitingStudents[i].getId())
                        .groupId(smallGroup.getId())
                        .build();
                MvcResult result = performPost(ENROLLMENTS_URL, request, adminToken)
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.waitingListPosition").value(i + 1))
                        .andReturn();
                enrollments[i] = fromResponse(result, EnrollmentResponse.class);
            }

            // When - Middle student (position 2) leaves
            String waiting2Token = authHelper.login(waitingStudents[1].getEmail(), authHelper.DEFAULT_PASSWORD);
            performDelete(BASE_URL + "/" + enrollments[1].getId(), waiting2Token)
                    .andExpect(status().isOk());

            // Then - Waiting list should have 2 students
            performGet(BASE_URL + "/group/" + smallGroup.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));

            // First student should still be in position 1
            performGet(ENROLLMENTS_URL + "/" + enrollments[0].getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.waitingListPosition").value(1));
        }
    }
}
