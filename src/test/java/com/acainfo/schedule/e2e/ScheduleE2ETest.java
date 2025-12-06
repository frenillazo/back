package com.acainfo.schedule.e2e;

import com.acainfo.group.domain.model.GroupType;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.CreateScheduleRequest;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.ScheduleResponse;
import com.acainfo.schedule.infrastructure.adapter.in.rest.dto.UpdateScheduleRequest;
import com.acainfo.shared.e2e.BaseE2ETest;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E Integration tests for Schedule module.
 * Tests CRUD operations for schedules.
 */
@DisplayName("Schedule E2E Tests")
class ScheduleE2ETest extends BaseE2ETest {

    private static final String BASE_URL = "/api/schedules";

    private String adminToken;
    private SubjectGroup testGroup;

    @BeforeEach
    void setUp() {
        adminToken = authHelper.getAdminToken();
        // Create subject + teacher + group for schedule tests
        Subject subject = dataHelper.createDefaultSubject();
        User teacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Schedule", "Teacher");
        testGroup = dataHelper.createGroup(subject.getId(), teacher.getId(), GroupType.REGULAR_Q1);
    }

    // ===========================================
    // Create Schedule Tests
    // ===========================================

    @Nested
    @DisplayName("POST /api/schedules")
    class CreateScheduleTests {

        @Test
        @DisplayName("Should create schedule as admin")
        void createSchedule_AsAdmin_ReturnsCreated() throws Exception {
            // Given
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.groupId").value(testGroup.getId()))
                    .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                    .andExpect(jsonPath("$.startTime").value("09:00"))
                    .andExpect(jsonPath("$.endTime").value("11:00"))
                    .andExpect(jsonPath("$.classroom").value("AULA_PORTAL1"))
                    .andExpect(jsonPath("$.durationMinutes").value(120));
        }

        @Test
        @DisplayName("Should create schedule in Aula Portal 2")
        void createSchedule_InAulaPortal2_ReturnsCreated() throws Exception {
            // Given
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.TUESDAY)
                    .startTime(LocalTime.of(16, 0))
                    .endTime(LocalTime.of(18, 0))
                    .classroom(Classroom.AULA_PORTAL2)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.classroom").value("AULA_PORTAL2"));
        }

        @Test
        @DisplayName("Should create virtual schedule")
        void createSchedule_Virtual_ReturnsCreated() throws Exception {
            // Given
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.WEDNESDAY)
                    .startTime(LocalTime.of(20, 0))
                    .endTime(LocalTime.of(22, 0))
                    .classroom(Classroom.AULA_VIRTUAL)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.classroom").value("AULA_VIRTUAL"));
        }

        @Test
        @DisplayName("Should reject schedule creation by student")
        void createSchedule_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject schedule with null group ID")
        void createSchedule_WithNullGroupId_ReturnsBadRequest() throws Exception {
            // Given
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(null)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject schedule with null day of week")
        void createSchedule_WithNullDayOfWeek_ReturnsBadRequest() throws Exception {
            // Given
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(null)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject schedule with null start time")
        void createSchedule_WithNullStartTime_ReturnsBadRequest() throws Exception {
            // Given
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(null)
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject schedule with null end time")
        void createSchedule_WithNullEndTime_ReturnsBadRequest() throws Exception {
            // Given
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(null)
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject schedule with null classroom")
        void createSchedule_WithNullClassroom_ReturnsBadRequest() throws Exception {
            // Given
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(null)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void createSchedule_WithoutToken_ReturnsForbidden() throws Exception {
            // Given
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();

            // When & Then
            performPost(BASE_URL, request)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject schedule with non-existent group")
        void createSchedule_WithNonExistentGroup_ReturnsNotFound() throws Exception {
            // Given
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(99999L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // Get Schedule Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/schedules/{id}")
    class GetScheduleByIdTests {

        @Test
        @DisplayName("Should get schedule by ID as authenticated user")
        void getScheduleById_AsAuthenticatedUser_ReturnsSchedule() throws Exception {
            // Given - Create schedule first
            CreateScheduleRequest createRequest = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            ScheduleResponse createdSchedule = fromResponse(createResult, ScheduleResponse.class);

            // When & Then - Get as student (any authenticated user can read)
            String studentToken = authHelper.getStudentToken();
            performGet(BASE_URL + "/" + createdSchedule.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdSchedule.getId()))
                    .andExpect(jsonPath("$.groupId").value(testGroup.getId()))
                    .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent schedule")
        void getScheduleById_WithInvalidId_ReturnsNotFound() throws Exception {
            // When & Then
            performGet(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void getScheduleById_WithoutToken_ReturnsForbidden() throws Exception {
            // When & Then
            performGet(BASE_URL + "/1")
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // List Schedules Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/schedules")
    class ListSchedulesTests {

        @Test
        @DisplayName("Should list schedules with pagination")
        void getSchedules_WithPagination_ReturnsPagedResults() throws Exception {
            // Given - Create schedules
            CreateScheduleRequest request1 = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            performPost(BASE_URL, request1, adminToken)
                    .andExpect(status().isCreated());

            CreateScheduleRequest request2 = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.WEDNESDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            performPost(BASE_URL, request2, adminToken)
                    .andExpect(status().isCreated());

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
        @DisplayName("Should filter schedules by group ID")
        void getSchedules_WithGroupIdFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create schedule
            CreateScheduleRequest request = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?groupId=" + testGroup.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter schedules by classroom")
        void getSchedules_WithClassroomFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create schedules with different classrooms
            CreateScheduleRequest portal1 = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            performPost(BASE_URL, portal1, adminToken)
                    .andExpect(status().isCreated());

            CreateScheduleRequest portal2 = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.TUESDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL2)
                    .build();
            performPost(BASE_URL, portal2, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?classroom=AULA_PORTAL1", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter schedules by day of week")
        void getSchedules_WithDayOfWeekFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create schedules with different days
            CreateScheduleRequest monday = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            performPost(BASE_URL, monday, adminToken)
                    .andExpect(status().isCreated());

            CreateScheduleRequest friday = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.FRIDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            performPost(BASE_URL, friday, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?dayOfWeek=MONDAY", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ===========================================
    // Get Schedules by Group Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/schedules/group/{groupId}")
    class GetSchedulesByGroupTests {

        @Test
        @DisplayName("Should get all schedules for a group")
        void getSchedulesByGroup_ReturnsScheduleList() throws Exception {
            // Given - Create schedules for the group
            CreateScheduleRequest request1 = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            performPost(BASE_URL, request1, adminToken)
                    .andExpect(status().isCreated());

            CreateScheduleRequest request2 = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.WEDNESDAY)
                    .startTime(LocalTime.of(16, 0))
                    .endTime(LocalTime.of(18, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            performPost(BASE_URL, request2, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            String studentToken = authHelper.getStudentToken();
            performGet(BASE_URL + "/group/" + testGroup.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should return empty list for group without schedules")
        void getSchedulesByGroup_NoSchedules_ReturnsEmptyList() throws Exception {
            // Given - Create a new group without schedules
            Subject subject = dataHelper.createDefaultSubject();
            User teacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Empty", "Teacher");
            SubjectGroup emptyGroup = dataHelper.createGroup(subject.getId(), teacher.getId(), GroupType.REGULAR_Q1);

            // When & Then
            performGet(BASE_URL + "/group/" + emptyGroup.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ===========================================
    // Update Schedule Tests
    // ===========================================

    @Nested
    @DisplayName("PUT /api/schedules/{id}")
    class UpdateScheduleTests {

        @Test
        @DisplayName("Should update schedule day of week as admin")
        void updateSchedule_DayChange_ReturnsUpdatedSchedule() throws Exception {
            // Given - Create schedule first
            CreateScheduleRequest createRequest = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            ScheduleResponse createdSchedule = fromResponse(createResult, ScheduleResponse.class);

            // When
            UpdateScheduleRequest updateRequest = UpdateScheduleRequest.builder()
                    .dayOfWeek(DayOfWeek.TUESDAY)
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdSchedule.getId(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdSchedule.getId()))
                    .andExpect(jsonPath("$.dayOfWeek").value("TUESDAY"));
        }

        @Test
        @DisplayName("Should update schedule times as admin")
        void updateSchedule_TimeChange_ReturnsUpdatedSchedule() throws Exception {
            // Given
            CreateScheduleRequest createRequest = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            ScheduleResponse createdSchedule = fromResponse(createResult, ScheduleResponse.class);

            // When
            UpdateScheduleRequest updateRequest = UpdateScheduleRequest.builder()
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(16, 0))
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdSchedule.getId(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime").value("14:00"))
                    .andExpect(jsonPath("$.endTime").value("16:00"))
                    .andExpect(jsonPath("$.durationMinutes").value(120));
        }

        @Test
        @DisplayName("Should update schedule classroom as admin")
        void updateSchedule_ClassroomChange_ReturnsUpdatedSchedule() throws Exception {
            // Given
            CreateScheduleRequest createRequest = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            ScheduleResponse createdSchedule = fromResponse(createResult, ScheduleResponse.class);

            // When
            UpdateScheduleRequest updateRequest = UpdateScheduleRequest.builder()
                    .classroom(Classroom.AULA_PORTAL2)
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdSchedule.getId(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.classroom").value("AULA_PORTAL2"));
        }

        @Test
        @DisplayName("Should reject update by student")
        void updateSchedule_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateScheduleRequest createRequest = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            ScheduleResponse createdSchedule = fromResponse(createResult, ScheduleResponse.class);

            // When
            UpdateScheduleRequest updateRequest = UpdateScheduleRequest.builder()
                    .dayOfWeek(DayOfWeek.FRIDAY)
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdSchedule.getId(), updateRequest, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent schedule")
        void updateSchedule_WithInvalidId_ReturnsNotFound() throws Exception {
            // Given
            UpdateScheduleRequest updateRequest = UpdateScheduleRequest.builder()
                    .dayOfWeek(DayOfWeek.FRIDAY)
                    .build();

            // When & Then
            performPut(BASE_URL + "/99999", updateRequest, adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // Delete Schedule Tests
    // ===========================================

    @Nested
    @DisplayName("DELETE /api/schedules/{id}")
    class DeleteScheduleTests {

        @Test
        @DisplayName("Should delete schedule as admin")
        void deleteSchedule_AsAdmin_ReturnsNoContent() throws Exception {
            // Given
            CreateScheduleRequest createRequest = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            ScheduleResponse createdSchedule = fromResponse(createResult, ScheduleResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdSchedule.getId(), adminToken)
                    .andExpect(status().isNoContent());

            // Verify schedule is deleted (should return 404)
            performGet(BASE_URL + "/" + createdSchedule.getId(), adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject delete by student")
        void deleteSchedule_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateScheduleRequest createRequest = CreateScheduleRequest.builder()
                    .groupId(testGroup.getId())
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            ScheduleResponse createdSchedule = fromResponse(createResult, ScheduleResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdSchedule.getId(), studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent schedule")
        void deleteSchedule_WithInvalidId_ReturnsNotFound() throws Exception {
            // When & Then
            performDelete(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }
    }
}
