package com.acainfo.subject.e2e;

import com.acainfo.shared.e2e.BaseE2ETest;
import com.acainfo.subject.domain.model.Degree;
import com.acainfo.subject.domain.model.SubjectStatus;
import com.acainfo.subject.infrastructure.adapter.in.rest.dto.CreateSubjectRequest;
import com.acainfo.subject.infrastructure.adapter.in.rest.dto.SubjectResponse;
import com.acainfo.subject.infrastructure.adapter.in.rest.dto.UpdateSubjectRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E Integration tests for Subject module.
 * Tests CRUD operations for subjects.
 */
@DisplayName("Subject E2E Tests")
class SubjectE2ETest extends BaseE2ETest {

    private static final String BASE_URL = "/api/subjects";

    // ===========================================
    // Create Subject Tests
    // ===========================================

    @Nested
    @DisplayName("POST /api/subjects")
    class CreateSubjectTests {

        @Test
        @DisplayName("Should create subject as admin")
        void createSubject_AsAdmin_ReturnsCreated() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            CreateSubjectRequest request = new CreateSubjectRequest(
                    "ING101",
                    "Programación I",
                    Degree.INGENIERIA_INFORMATICA
            );

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.code").value("ING101"))
                    .andExpect(jsonPath("$.name").value("Programación I"))
                    .andExpect(jsonPath("$.degree").value("INGENIERIA_INFORMATICA"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("Should reject subject creation by student")
        void createSubject_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateSubjectRequest request = new CreateSubjectRequest(
                    "ING102",
                    "Programación II",
                    Degree.INGENIERIA_INFORMATICA
            );

            // When & Then
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject duplicate subject code")
        void createSubject_WithDuplicateCode_ReturnsBadRequest() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            String code = "DUP001";

            CreateSubjectRequest request1 = new CreateSubjectRequest(
                    code,
                    "First Subject",
                    Degree.INGENIERIA_INFORMATICA
            );
            performPost(BASE_URL, request1, adminToken)
                    .andExpect(status().isCreated());

            // When - Try to create subject with same code
            CreateSubjectRequest request2 = new CreateSubjectRequest(
                    code,
                    "Second Subject",
                    Degree.INGENIERIA_INDUSTRIAL
            );

            // Then
            performPost(BASE_URL, request2, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject invalid subject code format")
        void createSubject_WithInvalidCodeFormat_ReturnsBadRequest() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            CreateSubjectRequest request = new CreateSubjectRequest(
                    "invalid-code",  // Should be 3 uppercase letters + 3 digits
                    "Test Subject",
                    Degree.INGENIERIA_INFORMATICA
            );

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject blank subject name")
        void createSubject_WithBlankName_ReturnsBadRequest() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            CreateSubjectRequest request = new CreateSubjectRequest(
                    "ING103",
                    "",
                    Degree.INGENIERIA_INFORMATICA
            );

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject null degree")
        void createSubject_WithNullDegree_ReturnsBadRequest() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            CreateSubjectRequest request = new CreateSubjectRequest(
                    "ING104",
                    "Test Subject",
                    null
            );

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void createSubject_WithoutToken_ReturnsForbidden() throws Exception {
            // Given
            CreateSubjectRequest request = new CreateSubjectRequest(
                    "ING105",
                    "Test Subject",
                    Degree.INGENIERIA_INFORMATICA
            );

            // When & Then
            performPost(BASE_URL, request)
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // Get Subject Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/subjects/{id}")
    class GetSubjectByIdTests {

        @Test
        @DisplayName("Should get subject by ID as authenticated user")
        void getSubjectById_AsAuthenticatedUser_ReturnsSubject() throws Exception {
            // Given - Create subject first
            String adminToken = authHelper.getAdminToken();
            CreateSubjectRequest createRequest = new CreateSubjectRequest(
                    "GET001",
                    "Test Subject",
                    Degree.INGENIERIA_INFORMATICA
            );
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SubjectResponse createdSubject = fromResponse(createResult, SubjectResponse.class);

            // When & Then - Get as student (any authenticated user can read)
            String studentToken = authHelper.getStudentToken();
            performGet(BASE_URL + "/" + createdSubject.id(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdSubject.id()))
                    .andExpect(jsonPath("$.code").value("GET001"))
                    .andExpect(jsonPath("$.name").value("Test Subject"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent subject")
        void getSubjectById_WithInvalidId_ReturnsNotFound() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // When & Then
            performGet(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void getSubjectById_WithoutToken_ReturnsForbidden() throws Exception {
            // When & Then
            performGet(BASE_URL + "/1")
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/subjects/code/{code}")
    class GetSubjectByCodeTests {

        @Test
        @DisplayName("Should get subject by code")
        void getSubjectByCode_WithValidCode_ReturnsSubject() throws Exception {
            // Given - Create subject first
            String adminToken = authHelper.getAdminToken();
            String code = "COD001";
            CreateSubjectRequest createRequest = new CreateSubjectRequest(
                    code,
                    "Test Subject by Code",
                    Degree.INGENIERIA_INDUSTRIAL
            );
            performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "/code/" + code, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(code))
                    .andExpect(jsonPath("$.name").value("Test Subject by Code"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent code")
        void getSubjectByCode_WithInvalidCode_ReturnsNotFound() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // When & Then
            performGet(BASE_URL + "/code/XXX999", adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // List Subjects Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/subjects")
    class ListSubjectsTests {

        @Test
        @DisplayName("Should list subjects with pagination")
        void getSubjects_WithPagination_ReturnsPagedResults() throws Exception {
            // Given - Create subjects
            String adminToken = authHelper.getAdminToken();
            for (int i = 1; i <= 3; i++) {
                CreateSubjectRequest request = new CreateSubjectRequest(
                        String.format("LST%03d", i),
                        "List Subject " + i,
                        Degree.INGENIERIA_INFORMATICA
                );
                performPost(BASE_URL, request, adminToken)
                        .andExpect(status().isCreated());
            }

            // When & Then
            String studentToken = authHelper.getStudentToken();
            performGet(BASE_URL + "?page=0&size=10", studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").exists())
                    .andExpect(jsonPath("$.page").exists())
                    .andExpect(jsonPath("$.size").exists());
        }

        @Test
        @DisplayName("Should filter subjects by degree")
        void getSubjects_WithDegreeFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create subjects with different degrees
            String adminToken = authHelper.getAdminToken();

            CreateSubjectRequest informaticaSubject = new CreateSubjectRequest(
                    "INF001",
                    "Informatics Subject",
                    Degree.INGENIERIA_INFORMATICA
            );
            performPost(BASE_URL, informaticaSubject, adminToken)
                    .andExpect(status().isCreated());

            CreateSubjectRequest industrialSubject = new CreateSubjectRequest(
                    "IND001",
                    "Industrial Subject",
                    Degree.INGENIERIA_INDUSTRIAL
            );
            performPost(BASE_URL, industrialSubject, adminToken)
                    .andExpect(status().isCreated());

            // When & Then - Filter by INGENIERIA_INFORMATICA
            performGet(BASE_URL + "?degree=INGENIERIA_INFORMATICA", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter subjects by status")
        void getSubjects_WithStatusFilter_ReturnsFilteredResults() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // When & Then
            performGet(BASE_URL + "?status=ACTIVE", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should search subjects by term")
        void getSubjects_WithSearchTerm_ReturnsMatchingResults() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            CreateSubjectRequest request = new CreateSubjectRequest(
                    "SRC001",
                    "Searchable Programming Course",
                    Degree.INGENIERIA_INFORMATICA
            );
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?searchTerm=Searchable", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ===========================================
    // Update Subject Tests
    // ===========================================

    @Nested
    @DisplayName("PUT /api/subjects/{id}")
    class UpdateSubjectTests {

        @Test
        @DisplayName("Should update subject as admin")
        void updateSubject_AsAdmin_ReturnsUpdatedSubject() throws Exception {
            // Given - Create subject first
            String adminToken = authHelper.getAdminToken();
            CreateSubjectRequest createRequest = new CreateSubjectRequest(
                    "UPD001",
                    "Original Name",
                    Degree.INGENIERIA_INFORMATICA
            );
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SubjectResponse createdSubject = fromResponse(createResult, SubjectResponse.class);

            // When
            UpdateSubjectRequest updateRequest = new UpdateSubjectRequest(
                    "Updated Name",
                    SubjectStatus.ACTIVE
            );

            // Then
            performPut(BASE_URL + "/" + createdSubject.id(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdSubject.id()))
                    .andExpect(jsonPath("$.name").value("Updated Name"))
                    .andExpect(jsonPath("$.code").value("UPD001"));  // Code should not change
        }

        @Test
        @DisplayName("Should update subject status")
        void updateSubject_StatusChange_ReturnsUpdatedSubject() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            CreateSubjectRequest createRequest = new CreateSubjectRequest(
                    "STS001",
                    "Status Test Subject",
                    Degree.INGENIERIA_INFORMATICA
            );
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SubjectResponse createdSubject = fromResponse(createResult, SubjectResponse.class);

            // When
            UpdateSubjectRequest updateRequest = new UpdateSubjectRequest(
                    null,
                    SubjectStatus.INACTIVE
            );

            // Then
            performPut(BASE_URL + "/" + createdSubject.id(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("INACTIVE"))
                    .andExpect(jsonPath("$.active").value(false));
        }

        @Test
        @DisplayName("Should reject update by student")
        void updateSubject_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            String studentToken = authHelper.getStudentToken();

            CreateSubjectRequest createRequest = new CreateSubjectRequest(
                    "FRB001",
                    "Forbidden Update Subject",
                    Degree.INGENIERIA_INFORMATICA
            );
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SubjectResponse createdSubject = fromResponse(createResult, SubjectResponse.class);

            // When
            UpdateSubjectRequest updateRequest = new UpdateSubjectRequest("New Name", null);

            // Then
            performPut(BASE_URL + "/" + createdSubject.id(), updateRequest, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent subject")
        void updateSubject_WithInvalidId_ReturnsNotFound() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            UpdateSubjectRequest updateRequest = new UpdateSubjectRequest("New Name", null);

            // When & Then
            performPut(BASE_URL + "/99999", updateRequest, adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // Delete Subject Tests
    // ===========================================

    @Nested
    @DisplayName("DELETE /api/subjects/{id}")
    class DeleteSubjectTests {

        @Test
        @DisplayName("Should delete subject as admin")
        void deleteSubject_AsAdmin_ReturnsOk() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            CreateSubjectRequest createRequest = new CreateSubjectRequest(
                    "DEL001",
                    "Delete Test Subject",
                    Degree.INGENIERIA_INFORMATICA
            );
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SubjectResponse createdSubject = fromResponse(createResult, SubjectResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdSubject.id(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Subject deleted successfully"));

            // Verify subject is deleted (should return 404)
            performGet(BASE_URL + "/" + createdSubject.id(), adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject delete by student")
        void deleteSubject_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            String studentToken = authHelper.getStudentToken();

            CreateSubjectRequest createRequest = new CreateSubjectRequest(
                    "NOD001",
                    "No Delete Subject",
                    Degree.INGENIERIA_INFORMATICA
            );
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SubjectResponse createdSubject = fromResponse(createResult, SubjectResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdSubject.id(), studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent subject")
        void deleteSubject_WithInvalidId_ReturnsNotFound() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();

            // When & Then
            performDelete(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // Archive Subject Tests
    // ===========================================

    @Nested
    @DisplayName("PUT /api/subjects/{id}/archive")
    class ArchiveSubjectTests {

        @Test
        @DisplayName("Should archive subject as admin")
        void archiveSubject_AsAdmin_ReturnsArchivedSubject() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            CreateSubjectRequest createRequest = new CreateSubjectRequest(
                    "ARC001",
                    "Archive Test Subject",
                    Degree.INGENIERIA_INFORMATICA
            );
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SubjectResponse createdSubject = fromResponse(createResult, SubjectResponse.class);

            // When & Then
            performPut(BASE_URL + "/" + createdSubject.id() + "/archive", null, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ARCHIVED"))
                    .andExpect(jsonPath("$.archived").value(true));
        }

        @Test
        @DisplayName("Should reject archive by student")
        void archiveSubject_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String adminToken = authHelper.getAdminToken();
            String studentToken = authHelper.getStudentToken();

            CreateSubjectRequest createRequest = new CreateSubjectRequest(
                    "NOA001",
                    "No Archive Subject",
                    Degree.INGENIERIA_INFORMATICA
            );
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SubjectResponse createdSubject = fromResponse(createResult, SubjectResponse.class);

            // When & Then
            performPut(BASE_URL + "/" + createdSubject.id() + "/archive", null, studentToken)
                    .andExpect(status().isForbidden());
        }
    }
}
