package com.acainfo.group.e2e;

import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.GroupType;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.CreateGroupRequest;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.GroupResponse;
import com.acainfo.group.infrastructure.adapter.in.rest.dto.UpdateGroupRequest;
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
 * E2E Integration tests for Group module.
 * Tests CRUD operations for groups.
 */
@DisplayName("Group E2E Tests")
class GroupE2ETest extends BaseE2ETest {

    private static final String BASE_URL = "/api/groups";

    private String adminToken;
    private Subject testSubject;
    private User testTeacher;

    @BeforeEach
    void setUp() {
        adminToken = authHelper.getAdminToken();
        testSubject = dataHelper.createDefaultSubject();
        testTeacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Test", "Teacher");
    }

    // ===========================================
    // Create Group Tests
    // ===========================================

    @Nested
    @DisplayName("POST /api/groups")
    class CreateGroupTests {

        @Test
        @DisplayName("Should create group as admin")
        void createGroup_AsAdmin_ReturnsCreated() throws Exception {
            // Given
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .capacity(20)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.subjectId").value(testSubject.getId()))
                    .andExpect(jsonPath("$.teacherId").value(testTeacher.getId()))
                    .andExpect(jsonPath("$.type").value("REGULAR_Q1"))
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.capacity").value(20))
                    .andExpect(jsonPath("$.isOpen").value(true))
                    .andExpect(jsonPath("$.isRegular").value(true));
        }

        @Test
        @DisplayName("Should create group with default capacity")
        void createGroup_WithDefaultCapacity_ReturnsCreated() throws Exception {
            // Given - No capacity specified, should use default based on type
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();

            // When & Then
            // capacity is null when using default, but maxCapacity reflects the effective value
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.capacity").isEmpty())  // null when using default
                    .andExpect(jsonPath("$.maxCapacity").value(24));  // Effective capacity for REGULAR
        }

        @Test
        @DisplayName("Should create intensive group with correct capacity")
        void createGroup_IntensiveType_ReturnsCreatedWithCorrectCapacity() throws Exception {
            // Given
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.INTENSIVE_Q1)
                    .build();

            // When & Then
            // capacity is null when using default, but maxCapacity reflects the effective value
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value("INTENSIVE_Q1"))
                    .andExpect(jsonPath("$.capacity").isEmpty())  // null when using default
                    .andExpect(jsonPath("$.maxCapacity").value(50))  // Effective capacity for INTENSIVE
                    .andExpect(jsonPath("$.isIntensive").value(true))
                    .andExpect(jsonPath("$.isRegular").value(false));
        }

        @Test
        @DisplayName("Should reject group creation by student")
        void createGroup_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject group with null subject ID")
        void createGroup_WithNullSubjectId_ReturnsBadRequest() throws Exception {
            // Given
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(null)
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject group with null teacher ID")
        void createGroup_WithNullTeacherId_ReturnsBadRequest() throws Exception {
            // Given
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(null)
                    .type(GroupType.REGULAR_Q1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject group with null type")
        void createGroup_WithNullType_ReturnsBadRequest() throws Exception {
            // Given
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(null)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject group with capacity less than 1")
        void createGroup_WithZeroCapacity_ReturnsBadRequest() throws Exception {
            // Given
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .capacity(0)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void createGroup_WithoutToken_ReturnsForbidden() throws Exception {
            // Given
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();

            // When & Then
            performPost(BASE_URL, request)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow multiple groups of same type for same subject")
        void createGroup_MultipleOfSameType_ReturnsCreated() throws Exception {
            // Given - Create first group
            CreateGroupRequest request1 = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            performPost(BASE_URL, request1, adminToken)
                    .andExpect(status().isCreated());

            // Create another teacher for second group
            User teacher2 = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Second", "Teacher");

            // When - Create second group of same type
            CreateGroupRequest request2 = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(teacher2.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();

            // Then - Should succeed (no uniqueness constraint on subject_id + type)
            performPost(BASE_URL, request2, adminToken)
                    .andExpect(status().isCreated());
        }
    }

    // ===========================================
    // Get Group Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/groups/{id}")
    class GetGroupByIdTests {

        @Test
        @DisplayName("Should get group by ID as authenticated user")
        void getGroupById_AsAuthenticatedUser_ReturnsGroup() throws Exception {
            // Given - Create group first
            CreateGroupRequest createRequest = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .capacity(20)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupResponse createdGroup = fromResponse(createResult, GroupResponse.class);

            // When & Then - Get as student (any authenticated user can read)
            String studentToken = authHelper.getStudentToken();
            performGet(BASE_URL + "/" + createdGroup.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdGroup.getId()))
                    .andExpect(jsonPath("$.subjectId").value(testSubject.getId()))
                    .andExpect(jsonPath("$.teacherId").value(testTeacher.getId()))
                    .andExpect(jsonPath("$.type").value("REGULAR_Q1"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent group")
        void getGroupById_WithInvalidId_ReturnsNotFound() throws Exception {
            // When & Then
            performGet(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void getGroupById_WithoutToken_ReturnsForbidden() throws Exception {
            // When & Then
            performGet(BASE_URL + "/1")
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // List Groups Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/groups")
    class ListGroupsTests {

        @Test
        @DisplayName("Should list groups with pagination")
        void getGroups_WithPagination_ReturnsPagedResults() throws Exception {
            // Given - Create groups
            for (int i = 0; i < 3; i++) {
                User teacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Teacher", String.valueOf(i));
                CreateGroupRequest request = CreateGroupRequest.builder()
                        .subjectId(testSubject.getId())
                        .teacherId(teacher.getId())
                        .type(GroupType.REGULAR_Q1)
                        .build();
                performPost(BASE_URL, request, adminToken)
                        .andExpect(status().isCreated());
            }

            // When & Then
            String studentToken = authHelper.getStudentToken();
            performGet(BASE_URL + "?page=0&size=10", studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").exists())
                    .andExpect(jsonPath("$.number").exists())
                    .andExpect(jsonPath("$.size").exists());
        }

        @Test
        @DisplayName("Should filter groups by subject ID")
        void getGroups_WithSubjectIdFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create group
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?subjectId=" + testSubject.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter groups by teacher ID")
        void getGroups_WithTeacherIdFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create group
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?teacherId=" + testTeacher.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter groups by type")
        void getGroups_WithTypeFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create groups with different types
            CreateGroupRequest regularGroup = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            performPost(BASE_URL, regularGroup, adminToken)
                    .andExpect(status().isCreated());

            User teacher2 = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Intensive", "Teacher");
            CreateGroupRequest intensiveGroup = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(teacher2.getId())
                    .type(GroupType.INTENSIVE_Q1)
                    .build();
            performPost(BASE_URL, intensiveGroup, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?type=REGULAR_Q1", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter groups by status")
        void getGroups_WithStatusFilter_ReturnsFilteredResults() throws Exception {
            // Given
            CreateGroupRequest request = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?status=OPEN", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ===========================================
    // Update Group Tests
    // ===========================================

    @Nested
    @DisplayName("PUT /api/groups/{id}")
    class UpdateGroupTests {

        @Test
        @DisplayName("Should update group capacity as admin")
        void updateGroup_CapacityChange_ReturnsUpdatedGroup() throws Exception {
            // Given - Create group first
            CreateGroupRequest createRequest = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .capacity(20)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupResponse createdGroup = fromResponse(createResult, GroupResponse.class);

            // When
            UpdateGroupRequest updateRequest = UpdateGroupRequest.builder()
                    .capacity(24)
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdGroup.getId(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdGroup.getId()))
                    .andExpect(jsonPath("$.capacity").value(24));
        }

        @Test
        @DisplayName("Should update group status as admin")
        void updateGroup_StatusChange_ReturnsUpdatedGroup() throws Exception {
            // Given
            CreateGroupRequest createRequest = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupResponse createdGroup = fromResponse(createResult, GroupResponse.class);

            // When
            UpdateGroupRequest updateRequest = UpdateGroupRequest.builder()
                    .status(GroupStatus.CLOSED)
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdGroup.getId(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CLOSED"))
                    .andExpect(jsonPath("$.isOpen").value(false));
        }

        @Test
        @DisplayName("Should reject update by student")
        void updateGroup_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateGroupRequest createRequest = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupResponse createdGroup = fromResponse(createResult, GroupResponse.class);

            // When
            UpdateGroupRequest updateRequest = UpdateGroupRequest.builder()
                    .capacity(30)
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdGroup.getId(), updateRequest, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent group")
        void updateGroup_WithInvalidId_ReturnsNotFound() throws Exception {
            // Given
            UpdateGroupRequest updateRequest = UpdateGroupRequest.builder()
                    .capacity(30)
                    .build();

            // When & Then
            performPut(BASE_URL + "/99999", updateRequest, adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // Delete Group Tests
    // ===========================================

    @Nested
    @DisplayName("DELETE /api/groups/{id}")
    class DeleteGroupTests {

        @Test
        @DisplayName("Should delete group as admin")
        void deleteGroup_AsAdmin_ReturnsNoContent() throws Exception {
            // Given
            CreateGroupRequest createRequest = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupResponse createdGroup = fromResponse(createResult, GroupResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdGroup.getId(), adminToken)
                    .andExpect(status().isNoContent());

            // Verify group is deleted (should return 404)
            performGet(BASE_URL + "/" + createdGroup.getId(), adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject delete by student")
        void deleteGroup_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateGroupRequest createRequest = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupResponse createdGroup = fromResponse(createResult, GroupResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdGroup.getId(), studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent group")
        void deleteGroup_WithInvalidId_ReturnsNotFound() throws Exception {
            // When & Then
            performDelete(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // Cancel Group Tests
    // ===========================================

    @Nested
    @DisplayName("POST /api/groups/{id}/cancel")
    class CancelGroupTests {

        @Test
        @DisplayName("Should cancel group as admin")
        void cancelGroup_AsAdmin_ReturnsCancelledGroup() throws Exception {
            // Given
            CreateGroupRequest createRequest = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupResponse createdGroup = fromResponse(createResult, GroupResponse.class);

            // When & Then
            performPost(BASE_URL + "/" + createdGroup.getId() + "/cancel", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"))
                    .andExpect(jsonPath("$.isOpen").value(false));
        }

        @Test
        @DisplayName("Should reject cancel by student")
        void cancelGroup_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateGroupRequest createRequest = CreateGroupRequest.builder()
                    .subjectId(testSubject.getId())
                    .teacherId(testTeacher.getId())
                    .type(GroupType.REGULAR_Q1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            GroupResponse createdGroup = fromResponse(createResult, GroupResponse.class);

            // When & Then
            performPost(BASE_URL + "/" + createdGroup.getId() + "/cancel", studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent group")
        void cancelGroup_WithInvalidId_ReturnsNotFound() throws Exception {
            // When & Then
            performPost(BASE_URL + "/99999/cancel", adminToken)
                    .andExpect(status().isNotFound());
        }
    }
}
