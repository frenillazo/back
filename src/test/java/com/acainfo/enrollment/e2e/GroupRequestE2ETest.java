package com.acainfo.enrollment.e2e;

import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.AddSupporterRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.CreateGroupRequestRequest;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.GroupRequestResponse;
import com.acainfo.enrollment.infrastructure.adapter.in.rest.dto.ProcessGroupRequestRequest;
import com.acainfo.group.domain.model.GroupType;
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
 * E2E Integration tests for Group Request operations.
 * Tests CRUD operations for group requests including supporter management and admin processing.
 */
@DisplayName("Group Request E2E Tests")
class GroupRequestE2ETest extends BaseE2ETest {

    private static final String BASE_URL = "/api/group-requests";

    private String adminToken;
    private String studentToken;
    private User testStudent;
    private Subject testSubject;

    @BeforeEach
    void setUp() {
        adminToken = authHelper.getAdminToken();
        testStudent = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Request", "Student");
        studentToken = authHelper.login(testStudent.getEmail(), authHelper.DEFAULT_PASSWORD);
        testSubject = dataHelper.createDefaultSubject();
    }

    // ===========================================
    // Create Group Request Tests
    // ===========================================

    @Nested
    @DisplayName("POST /api/group-requests")
    class CreateGroupRequestTests {

        @Test
        @DisplayName("Should create group request with requester as first supporter")
        void createGroupRequest_AsStudent_ReturnsCreated() throws Exception {
            // Given
            CreateGroupRequestRequest request = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .justification("We need more group options for this subject")
                    .build();

            // When & Then
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.subjectId").value(testSubject.getId()))
                    .andExpect(jsonPath("$.requesterId").value(testStudent.getId()))
                    .andExpect(jsonPath("$.requestedGroupType").value("REGULAR_Q1"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.justification").value("We need more group options for this subject"))
                    .andExpect(jsonPath("$.supporterCount").value(1))
                    .andExpect(jsonPath("$.hasMinimumSupporters").value(false))
                    .andExpect(jsonPath("$.supportersNeeded").value(7))
                    .andExpect(jsonPath("$.isPending").value(true))
                    .andExpect(jsonPath("$.expiresAt").exists());
        }

        @Test
        @DisplayName("Should create group request as admin")
        void createGroupRequest_AsAdmin_ReturnsCreated() throws Exception {
            // Given
            CreateGroupRequestRequest request = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.INTENSIVE_Q1)
                    .justification("Admin-created request for intensive group")
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.requestedGroupType").value("INTENSIVE_Q1"));
        }

        @Test
        @DisplayName("Should reject group request with null subject ID")
        void createGroupRequest_WithNullSubjectId_ReturnsBadRequest() throws Exception {
            // Given
            CreateGroupRequestRequest request = CreateGroupRequestRequest.builder()
                    .subjectId(null)
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject group request with null requester ID")
        void createGroupRequest_WithNullRequesterId_ReturnsBadRequest() throws Exception {
            // Given
            CreateGroupRequestRequest request = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(null)
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject group request with null group type")
        void createGroupRequest_WithNullGroupType_ReturnsBadRequest() throws Exception {
            // Given
            CreateGroupRequestRequest request = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(null)
                    .build();

            // When & Then
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject group request for non-existent subject")
        void createGroupRequest_WithInvalidSubject_ReturnsNotFound() throws Exception {
            // Given
            CreateGroupRequestRequest request = CreateGroupRequestRequest.builder()
                    .subjectId(99999L)
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void createGroupRequest_WithoutToken_ReturnsForbidden() throws Exception {
            // Given
            CreateGroupRequestRequest request = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();

            // When & Then
            performPost(BASE_URL, request)
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // Get Group Request Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/group-requests/{id}")
    class GetGroupRequestByIdTests {

        @Test
        @DisplayName("Should get group request by ID")
        void getGroupRequestById_WhenExists_ReturnsGroupRequest() throws Exception {
            // Given - Create group request first
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupRequestResponse createdRequest = fromResponse(createResult, GroupRequestResponse.class);

            // When & Then
            performGet(BASE_URL + "/" + createdRequest.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdRequest.getId()))
                    .andExpect(jsonPath("$.subjectId").value(testSubject.getId()))
                    .andExpect(jsonPath("$.requesterId").value(testStudent.getId()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent group request")
        void getGroupRequestById_WhenNotFound_ReturnsNotFound() throws Exception {
            // When & Then
            performGet(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void getGroupRequestById_WithoutToken_ReturnsForbidden() throws Exception {
            // When & Then
            performGet(BASE_URL + "/1")
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // List Group Requests Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/group-requests")
    class ListGroupRequestsTests {

        @Test
        @DisplayName("Should list group requests with pagination")
        void getGroupRequests_WithPagination_ReturnsPagedResults() throws Exception {
            // Given - Create group request
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            performPost(BASE_URL, createRequest, studentToken)
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
        @DisplayName("Should filter group requests by subject ID")
        void getGroupRequests_WithSubjectIdFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create group request
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?subjectId=" + testSubject.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter group requests by status")
        void getGroupRequests_WithStatusFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create group request
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?status=PENDING", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ===========================================
    // Add Supporter Tests
    // ===========================================

    @Nested
    @DisplayName("POST /api/group-requests/{id}/support")
    class AddSupporterTests {

        @Test
        @DisplayName("Should add supporter to pending request")
        void addSupporter_ToPendingRequest_ReturnsUpdatedRequest() throws Exception {
            // Given - Create group request
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupRequestResponse createdRequest = fromResponse(createResult, GroupRequestResponse.class);

            // Create another student as supporter
            User supporter = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Supporter", "Student");
            String supporterToken = authHelper.login(supporter.getEmail(), authHelper.DEFAULT_PASSWORD);

            AddSupporterRequest supportRequest = AddSupporterRequest.builder()
                    .studentId(supporter.getId())
                    .build();

            // When & Then
            performPost(BASE_URL + "/" + createdRequest.getId() + "/support", supportRequest, supporterToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.supporterCount").value(2))
                    .andExpect(jsonPath("$.supportersNeeded").value(6));
        }

        @Test
        @DisplayName("Should reject adding same supporter twice")
        void addSupporter_WhenAlreadySupporter_ReturnsConflict() throws Exception {
            // Given - Create group request (requester is already a supporter)
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupRequestResponse createdRequest = fromResponse(createResult, GroupRequestResponse.class);

            // Try to add requester again as supporter
            AddSupporterRequest supportRequest = AddSupporterRequest.builder()
                    .studentId(testStudent.getId())
                    .build();

            // When & Then
            performPost(BASE_URL + "/" + createdRequest.getId() + "/support", supportRequest, studentToken)
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should reject adding supporter to non-existent request")
        void addSupporter_WhenRequestNotFound_ReturnsNotFound() throws Exception {
            // Given
            AddSupporterRequest supportRequest = AddSupporterRequest.builder()
                    .studentId(testStudent.getId())
                    .build();

            // When & Then
            performPost(BASE_URL + "/99999/support", supportRequest, studentToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject unauthenticated support request")
        void addSupporter_WithoutToken_ReturnsForbidden() throws Exception {
            // Given
            AddSupporterRequest supportRequest = AddSupporterRequest.builder()
                    .studentId(testStudent.getId())
                    .build();

            // When & Then
            performPost(BASE_URL + "/1/support", supportRequest)
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // Remove Supporter Tests
    // ===========================================

    @Nested
    @DisplayName("DELETE /api/group-requests/{id}/support/{studentId}")
    class RemoveSupporterTests {

        @Test
        @DisplayName("Should remove supporter from pending request")
        void removeSupporter_FromPendingRequest_ReturnsUpdatedRequest() throws Exception {
            // Given - Create group request
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupRequestResponse createdRequest = fromResponse(createResult, GroupRequestResponse.class);

            // Add a supporter
            User supporter = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Supporter", "Student");
            String supporterToken = authHelper.login(supporter.getEmail(), authHelper.DEFAULT_PASSWORD);

            AddSupporterRequest supportRequest = AddSupporterRequest.builder()
                    .studentId(supporter.getId())
                    .build();
            performPost(BASE_URL + "/" + createdRequest.getId() + "/support", supportRequest, supporterToken)
                    .andExpect(status().isOk());

            // When & Then - Remove the supporter
            performDelete(BASE_URL + "/" + createdRequest.getId() + "/support/" + supporter.getId(), supporterToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.supporterCount").value(1));
        }

        @Test
        @DisplayName("Should reject removing requester from supporters")
        void removeSupporter_WhenRequester_ReturnsBadRequest() throws Exception {
            // Given - Create group request
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupRequestResponse createdRequest = fromResponse(createResult, GroupRequestResponse.class);

            // When & Then - Try to remove the requester
            performDelete(BASE_URL + "/" + createdRequest.getId() + "/support/" + testStudent.getId(), studentToken)
                    .andExpect(status().isBadRequest());
        }
    }

    // ===========================================
    // Get Supporters Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/group-requests/{id}/supporters")
    class GetSupportersTests {

        @Test
        @DisplayName("Should return list of supporters")
        void getSupporters_ReturnsSetOfSupporters() throws Exception {
            // Given - Create group request
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupRequestResponse createdRequest = fromResponse(createResult, GroupRequestResponse.class);

            // Add a supporter
            User supporter = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Supporter", "Student");
            String supporterToken = authHelper.login(supporter.getEmail(), authHelper.DEFAULT_PASSWORD);

            AddSupporterRequest supportRequest = AddSupporterRequest.builder()
                    .studentId(supporter.getId())
                    .build();
            performPost(BASE_URL + "/" + createdRequest.getId() + "/support", supportRequest, supporterToken)
                    .andExpect(status().isOk());

            // When & Then
            performGet(BASE_URL + "/" + createdRequest.getId() + "/supporters", studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    // ===========================================
    // Approve Group Request Tests
    // ===========================================

    @Nested
    @DisplayName("PUT /api/group-requests/{id}/approve")
    class ApproveGroupRequestTests {

        @Test
        @DisplayName("Should approve request with minimum supporters and create group")
        void approve_WithMinSupporters_CreatesGroupAndUpdatesRequest() throws Exception {
            // Given - Create group request with 8 supporters (minimum required)
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .justification("Need more availability")
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupRequestResponse createdRequest = fromResponse(createResult, GroupRequestResponse.class);

            // Add 7 more supporters (requester is already 1)
            for (int i = 0; i < 7; i++) {
                User supporter = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "Supporter" + i, "Student");
                String supporterToken = authHelper.login(supporter.getEmail(), authHelper.DEFAULT_PASSWORD);

                AddSupporterRequest supportRequest = AddSupporterRequest.builder()
                        .studentId(supporter.getId())
                        .build();
                performPost(BASE_URL + "/" + createdRequest.getId() + "/support", supportRequest, supporterToken)
                        .andExpect(status().isOk());
            }

            // Verify we have minimum supporters
            performGet(BASE_URL + "/" + createdRequest.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.supporterCount").value(8))
                    .andExpect(jsonPath("$.hasMinimumSupporters").value(true));

            // Get admin user for the approval
            ProcessGroupRequestRequest approveRequest = ProcessGroupRequestRequest.builder()
                    .adminId(1L) // Admin ID from data-test.sql
                    .adminResponse("Approved - high demand confirmed")
                    .build();

            // When & Then
            performPut(BASE_URL + "/" + createdRequest.getId() + "/approve", approveRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.createdGroupId").exists())
                    .andExpect(jsonPath("$.adminResponse").value("Approved - high demand confirmed"))
                    .andExpect(jsonPath("$.processedByAdminId").value(1))
                    .andExpect(jsonPath("$.processedAt").exists())
                    .andExpect(jsonPath("$.isApproved").value(true))
                    .andExpect(jsonPath("$.isProcessed").value(true));
        }

        @Test
        @DisplayName("Should reject approval when insufficient supporters")
        void approve_WithInsufficientSupporters_ReturnsBadRequest() throws Exception {
            // Given - Create group request with only 1 supporter (requester)
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupRequestResponse createdRequest = fromResponse(createResult, GroupRequestResponse.class);

            ProcessGroupRequestRequest approveRequest = ProcessGroupRequestRequest.builder()
                    .adminId(1L)
                    .adminResponse("Trying to approve")
                    .build();

            // When & Then
            performPut(BASE_URL + "/" + createdRequest.getId() + "/approve", approveRequest, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject approval by non-admin")
        void approve_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            ProcessGroupRequestRequest approveRequest = ProcessGroupRequestRequest.builder()
                    .adminId(testStudent.getId())
                    .adminResponse("Student trying to approve")
                    .build();

            // When & Then
            performPut(BASE_URL + "/1/approve", approveRequest, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject approval for non-existent request")
        void approve_WhenRequestNotFound_ReturnsNotFound() throws Exception {
            // Given
            ProcessGroupRequestRequest approveRequest = ProcessGroupRequestRequest.builder()
                    .adminId(1L)
                    .adminResponse("Approved")
                    .build();

            // When & Then
            performPut(BASE_URL + "/99999/approve", approveRequest, adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // Reject Group Request Tests
    // ===========================================

    @Nested
    @DisplayName("PUT /api/group-requests/{id}/reject")
    class RejectGroupRequestTests {

        @Test
        @DisplayName("Should reject pending request")
        void reject_WhenPending_RejectsRequest() throws Exception {
            // Given - Create group request
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupRequestResponse createdRequest = fromResponse(createResult, GroupRequestResponse.class);

            ProcessGroupRequestRequest rejectRequest = ProcessGroupRequestRequest.builder()
                    .adminId(1L)
                    .adminResponse("Not enough demand at this time")
                    .build();

            // When & Then
            performPut(BASE_URL + "/" + createdRequest.getId() + "/reject", rejectRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"))
                    .andExpect(jsonPath("$.adminResponse").value("Not enough demand at this time"))
                    .andExpect(jsonPath("$.processedByAdminId").value(1))
                    .andExpect(jsonPath("$.processedAt").exists())
                    .andExpect(jsonPath("$.isRejected").value(true))
                    .andExpect(jsonPath("$.isProcessed").value(true))
                    .andExpect(jsonPath("$.createdGroupId").isEmpty());
        }

        @Test
        @DisplayName("Should reject rejection by non-admin")
        void reject_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            ProcessGroupRequestRequest rejectRequest = ProcessGroupRequestRequest.builder()
                    .adminId(testStudent.getId())
                    .adminResponse("Student trying to reject")
                    .build();

            // When & Then
            performPut(BASE_URL + "/1/reject", rejectRequest, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject rejection for non-existent request")
        void reject_WhenRequestNotFound_ReturnsNotFound() throws Exception {
            // Given
            ProcessGroupRequestRequest rejectRequest = ProcessGroupRequestRequest.builder()
                    .adminId(1L)
                    .adminResponse("Rejected")
                    .build();

            // When & Then
            performPut(BASE_URL + "/99999/reject", rejectRequest, adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // Full Flow Tests
    // ===========================================

    @Nested
    @DisplayName("Full Group Request Flow")
    class FullFlowTests {

        @Test
        @DisplayName("Should complete full request flow from creation to approval")
        void fullFlow_CreateSupportApprove_Success() throws Exception {
            // Step 1: Create group request
            CreateGroupRequestRequest createRequest = CreateGroupRequestRequest.builder()
                    .subjectId(testSubject.getId())
                    .requesterId(testStudent.getId())
                    .requestedGroupType(GroupType.REGULAR_Q2)
                    .justification("Full flow test - need Q2 group")
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, studentToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.supporterCount").value(1))
                    .andReturn();

            GroupRequestResponse request = fromResponse(createResult, GroupRequestResponse.class);

            // Step 2: Add supporters until minimum (8)
            for (int i = 0; i < 7; i++) {
                User supporter = authHelper.registerStudent(authHelper.uniqueStudentEmail(), "FlowSupporter" + i, "Student");
                String supporterToken = authHelper.login(supporter.getEmail(), authHelper.DEFAULT_PASSWORD);

                AddSupporterRequest supportReq = AddSupporterRequest.builder()
                        .studentId(supporter.getId())
                        .build();
                performPost(BASE_URL + "/" + request.getId() + "/support", supportReq, supporterToken)
                        .andExpect(status().isOk());
            }

            // Step 3: Verify has minimum supporters
            performGet(BASE_URL + "/" + request.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.supporterCount").value(8))
                    .andExpect(jsonPath("$.hasMinimumSupporters").value(true))
                    .andExpect(jsonPath("$.supportersNeeded").value(0));

            // Step 4: Admin approves
            ProcessGroupRequestRequest approveRequest = ProcessGroupRequestRequest.builder()
                    .adminId(1L)
                    .adminResponse("Approved after full review")
                    .build();
            MvcResult approveResult = performPut(BASE_URL + "/" + request.getId() + "/approve", approveRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.createdGroupId").exists())
                    .andReturn();

            GroupRequestResponse approvedRequest = fromResponse(approveResult, GroupRequestResponse.class);

            // Step 5: Verify the created group exists
            performGet("/api/groups/" + approvedRequest.getCreatedGroupId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subjectId").value(testSubject.getId()))
                    .andExpect(jsonPath("$.type").value("REGULAR_Q2"));
        }
    }
}
