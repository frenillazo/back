package com.acainfo.session.e2e;

import com.acainfo.group.domain.model.GroupType;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionType;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.CreateSessionRequest;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.PostponeSessionRequest;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.SessionResponse;
import com.acainfo.session.infrastructure.adapter.in.rest.dto.UpdateSessionRequest;
import com.acainfo.shared.e2e.BaseE2ETest;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E Integration tests for Session module.
 * Tests CRUD operations and lifecycle state transitions for sessions.
 */
@DisplayName("Session E2E Tests")
class SessionE2ETest extends BaseE2ETest {

    private static final String BASE_URL = "/api/sessions";

    private String adminToken;
    private String teacherToken;
    private Subject testSubject;
    private SubjectGroup testGroup;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        adminToken = authHelper.getAdminToken();
        // Create subject + teacher + group + schedule for session tests
        testSubject = dataHelper.createDefaultSubject();
        User teacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Session", "Teacher");
        teacherToken = authHelper.login(teacher.getEmail(), authHelper.DEFAULT_PASSWORD);
        testGroup = dataHelper.createGroup(testSubject.getId(), teacher.getId(), GroupType.REGULAR_Q1);
        testSchedule = dataHelper.createSchedule(testGroup.getId(), DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), Classroom.AULA_PORTAL1);
    }

    // ===========================================
    // Create Session Tests
    // ===========================================

    @Nested
    @DisplayName("POST /api/sessions")
    class CreateSessionTests {

        @Test
        @DisplayName("Should create REGULAR session as admin")
        void createSession_RegularTypeAsAdmin_ReturnsCreated() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.type").value("REGULAR"))
                    .andExpect(jsonPath("$.status").value("SCHEDULED"))
                    .andExpect(jsonPath("$.groupId").value(testGroup.getId()))
                    .andExpect(jsonPath("$.scheduleId").value(testSchedule.getId()))
                    .andExpect(jsonPath("$.classroom").value("AULA_PORTAL1"))
                    .andExpect(jsonPath("$.mode").value("IN_PERSON"))
                    .andExpect(jsonPath("$.startTime").value("09:00"))
                    .andExpect(jsonPath("$.endTime").value("11:00"))
                    .andExpect(jsonPath("$.durationMinutes").value(120))
                    .andExpect(jsonPath("$.isScheduled").value(true))
                    .andExpect(jsonPath("$.isRegular").value(true))
                    .andExpect(jsonPath("$.hasGroup").value(true))
                    .andExpect(jsonPath("$.hasSchedule").value(true));
        }

        @Test
        @DisplayName("Should create REGULAR session as teacher")
        void createSession_RegularTypeAsTeacher_ReturnsCreated() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();

            // When & Then
            performPost(BASE_URL, request, teacherToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.type").value("REGULAR"));
        }

        @Test
        @DisplayName("Should create EXTRA session with group")
        void createSession_ExtraType_ReturnsCreated() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL2)
                    .date(LocalDate.now().plusDays(14))
                    .startTime(LocalTime.of(16, 0))
                    .endTime(LocalTime.of(18, 0))
                    .mode(SessionMode.DUAL)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value("EXTRA"))
                    .andExpect(jsonPath("$.groupId").value(testGroup.getId()))
                    .andExpect(jsonPath("$.scheduleId").isEmpty())
                    .andExpect(jsonPath("$.isExtra").value(true))
                    .andExpect(jsonPath("$.hasGroup").value(true))
                    .andExpect(jsonPath("$.hasSchedule").value(false));
        }

        @Test
        @DisplayName("Should create SCHEDULING session for subject")
        void createSession_SchedulingType_ReturnsCreated() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.SCHEDULING)
                    .subjectId(testSubject.getId())
                    .classroom(Classroom.AULA_VIRTUAL)
                    .date(LocalDate.now().plusDays(3))
                    .startTime(LocalTime.of(20, 0))
                    .endTime(LocalTime.of(21, 0))
                    .mode(SessionMode.ONLINE)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value("SCHEDULING"))
                    .andExpect(jsonPath("$.subjectId").value(testSubject.getId()))
                    .andExpect(jsonPath("$.groupId").isEmpty())
                    .andExpect(jsonPath("$.scheduleId").isEmpty())
                    .andExpect(jsonPath("$.mode").value("ONLINE"))
                    .andExpect(jsonPath("$.classroom").value("AULA_VIRTUAL"))
                    .andExpect(jsonPath("$.isSchedulingType").value(true))
                    .andExpect(jsonPath("$.hasGroup").value(false));
        }

        @Test
        @DisplayName("Should create online session")
        void createSession_OnlineMode_ReturnsCreated() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_VIRTUAL)
                    .date(LocalDate.now().plusDays(10))
                    .startTime(LocalTime.of(20, 0))
                    .endTime(LocalTime.of(22, 0))
                    .mode(SessionMode.ONLINE)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.mode").value("ONLINE"))
                    .andExpect(jsonPath("$.classroom").value("AULA_VIRTUAL"));
        }

        @Test
        @DisplayName("Should reject session creation by student")
        void createSession_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();

            // When & Then
            performPost(BASE_URL, request, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject session with null type")
        void createSession_WithNullType_ReturnsBadRequest() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(null)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject session with null classroom")
        void createSession_WithNullClassroom_ReturnsBadRequest() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(null)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject session with null date")
        void createSession_WithNullDate_ReturnsBadRequest() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(null)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject session with null start time")
        void createSession_WithNullStartTime_ReturnsBadRequest() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(null)
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject session with null end time")
        void createSession_WithNullEndTime_ReturnsBadRequest() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(null)
                    .mode(SessionMode.IN_PERSON)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject session with null mode")
        void createSession_WithNullMode_ReturnsBadRequest() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(null)
                    .build();

            // When & Then
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void createSession_WithoutToken_ReturnsForbidden() throws Exception {
            // Given
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();

            // When & Then
            performPost(BASE_URL, request)
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // Get Session Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/sessions/{id}")
    class GetSessionByIdTests {

        @Test
        @DisplayName("Should get session by ID as authenticated user")
        void getSessionById_AsAuthenticatedUser_ReturnsSession() throws Exception {
            // Given - Create session first
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When & Then - Get as student (any authenticated user can read)
            String studentToken = authHelper.getStudentToken();
            performGet(BASE_URL + "/" + createdSession.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdSession.getId()))
                    .andExpect(jsonPath("$.type").value("REGULAR"))
                    .andExpect(jsonPath("$.groupId").value(testGroup.getId()));
        }

        @Test
        @DisplayName("Should return 404 for non-existent session")
        void getSessionById_WithInvalidId_ReturnsNotFound() throws Exception {
            // When & Then
            performGet(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject unauthenticated request")
        void getSessionById_WithoutToken_ReturnsForbidden() throws Exception {
            // When & Then
            performGet(BASE_URL + "/1")
                    .andExpect(status().isForbidden());
        }
    }

    // ===========================================
    // List Sessions Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/sessions")
    class ListSessionsTests {

        @Test
        @DisplayName("Should list sessions with pagination")
        void getSessions_WithPagination_ReturnsPagedResults() throws Exception {
            // Given - Create sessions
            CreateSessionRequest request1 = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            performPost(BASE_URL, request1, adminToken)
                    .andExpect(status().isCreated());

            CreateSessionRequest request2 = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(14))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
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
        @DisplayName("Should filter sessions by group ID")
        void getSessions_WithGroupIdFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create session
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?groupId=" + testGroup.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter sessions by type")
        void getSessions_WithTypeFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create sessions with different types
            CreateSessionRequest regularSession = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            performPost(BASE_URL, regularSession, adminToken)
                    .andExpect(status().isCreated());

            CreateSessionRequest extraSession = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL2)
                    .date(LocalDate.now().plusDays(14))
                    .startTime(LocalTime.of(16, 0))
                    .endTime(LocalTime.of(18, 0))
                    .mode(SessionMode.DUAL)
                    .build();
            performPost(BASE_URL, extraSession, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?type=REGULAR", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter sessions by status")
        void getSessions_WithStatusFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create session (default status is SCHEDULED)
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?status=SCHEDULED", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter sessions by mode")
        void getSessions_WithModeFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create sessions with different modes
            CreateSessionRequest inPersonSession = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            performPost(BASE_URL, inPersonSession, adminToken)
                    .andExpect(status().isCreated());

            CreateSessionRequest onlineSession = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_VIRTUAL)
                    .date(LocalDate.now().plusDays(14))
                    .startTime(LocalTime.of(20, 0))
                    .endTime(LocalTime.of(22, 0))
                    .mode(SessionMode.ONLINE)
                    .build();
            performPost(BASE_URL, onlineSession, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "?mode=ONLINE", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should filter sessions by date range")
        void getSessions_WithDateRangeFilter_ReturnsFilteredResults() throws Exception {
            // Given - Create session
            LocalDate sessionDate = LocalDate.now().plusDays(10);
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(sessionDate)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            LocalDate dateFrom = LocalDate.now().plusDays(5);
            LocalDate dateTo = LocalDate.now().plusDays(15);
            performGet(BASE_URL + "?dateFrom=" + dateFrom + "&dateTo=" + dateTo, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ===========================================
    // Get Sessions by Group Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/sessions/group/{groupId}")
    class GetSessionsByGroupTests {

        @Test
        @DisplayName("Should get all sessions for a group")
        void getSessionsByGroup_ReturnsSessionList() throws Exception {
            // Given - Create sessions for the group
            CreateSessionRequest request1 = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            performPost(BASE_URL, request1, adminToken)
                    .andExpect(status().isCreated());

            CreateSessionRequest request2 = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(14))
                    .startTime(LocalTime.of(16, 0))
                    .endTime(LocalTime.of(18, 0))
                    .mode(SessionMode.DUAL)
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
        @DisplayName("Should return empty list for group without sessions")
        void getSessionsByGroup_NoSessions_ReturnsEmptyList() throws Exception {
            // Given - Create a new group without sessions
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
    // Get Sessions by Subject Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/sessions/subject/{subjectId}")
    class GetSessionsBySubjectTests {

        @Test
        @DisplayName("Should get all sessions for a subject")
        void getSessionsBySubject_ReturnsSessionList() throws Exception {
            // Given - Create session for the subject
            CreateSessionRequest request = CreateSessionRequest.builder()
                    .type(SessionType.SCHEDULING)
                    .subjectId(testSubject.getId())
                    .classroom(Classroom.AULA_VIRTUAL)
                    .date(LocalDate.now().plusDays(3))
                    .startTime(LocalTime.of(20, 0))
                    .endTime(LocalTime.of(21, 0))
                    .mode(SessionMode.ONLINE)
                    .build();
            performPost(BASE_URL, request, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            String studentToken = authHelper.getStudentToken();
            performGet(BASE_URL + "/subject/" + testSubject.getId(), studentToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ===========================================
    // Get Sessions by Schedule Tests
    // ===========================================

    @Nested
    @DisplayName("GET /api/sessions/schedule/{scheduleId}")
    class GetSessionsByScheduleTests {

        @Test
        @DisplayName("Should get all sessions for a schedule")
        void getSessionsBySchedule_ReturnsSessionList() throws Exception {
            // Given - Create sessions for the schedule
            CreateSessionRequest request1 = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            performPost(BASE_URL, request1, adminToken)
                    .andExpect(status().isCreated());

            CreateSessionRequest request2 = CreateSessionRequest.builder()
                    .type(SessionType.REGULAR)
                    .scheduleId(testSchedule.getId())
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(14))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            performPost(BASE_URL, request2, adminToken)
                    .andExpect(status().isCreated());

            // When & Then
            performGet(BASE_URL + "/schedule/" + testSchedule.getId(), adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    // ===========================================
    // Update Session Tests
    // ===========================================

    @Nested
    @DisplayName("PUT /api/sessions/{id}")
    class UpdateSessionTests {

        @Test
        @DisplayName("Should update session classroom as admin")
        void updateSession_ClassroomChange_ReturnsUpdatedSession() throws Exception {
            // Given - Create session first
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When
            UpdateSessionRequest updateRequest = UpdateSessionRequest.builder()
                    .classroom(Classroom.AULA_PORTAL2)
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdSession.getId(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdSession.getId()))
                    .andExpect(jsonPath("$.classroom").value("AULA_PORTAL2"));
        }

        @Test
        @DisplayName("Should update session times as teacher")
        void updateSession_TimeChange_ReturnsUpdatedSession() throws Exception {
            // Given
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When
            UpdateSessionRequest updateRequest = UpdateSessionRequest.builder()
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(16, 0))
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdSession.getId(), updateRequest, teacherToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.startTime").value("14:00"))
                    .andExpect(jsonPath("$.endTime").value("16:00"))
                    .andExpect(jsonPath("$.durationMinutes").value(120));
        }

        @Test
        @DisplayName("Should update session date")
        void updateSession_DateChange_ReturnsUpdatedSession() throws Exception {
            // Given
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);
            LocalDate newDate = LocalDate.now().plusDays(10);

            // When
            UpdateSessionRequest updateRequest = UpdateSessionRequest.builder()
                    .date(newDate)
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdSession.getId(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date").value(newDate.toString()));
        }

        @Test
        @DisplayName("Should update session mode")
        void updateSession_ModeChange_ReturnsUpdatedSession() throws Exception {
            // Given
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When
            UpdateSessionRequest updateRequest = UpdateSessionRequest.builder()
                    .mode(SessionMode.DUAL)
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdSession.getId(), updateRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mode").value("DUAL"));
        }

        @Test
        @DisplayName("Should reject update by student")
        void updateSession_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When
            UpdateSessionRequest updateRequest = UpdateSessionRequest.builder()
                    .classroom(Classroom.AULA_PORTAL2)
                    .build();

            // Then
            performPut(BASE_URL + "/" + createdSession.getId(), updateRequest, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent session")
        void updateSession_WithInvalidId_ReturnsNotFound() throws Exception {
            // Given
            UpdateSessionRequest updateRequest = UpdateSessionRequest.builder()
                    .classroom(Classroom.AULA_PORTAL2)
                    .build();

            // When & Then
            performPut(BASE_URL + "/99999", updateRequest, adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // Delete Session Tests
    // ===========================================

    @Nested
    @DisplayName("DELETE /api/sessions/{id}")
    class DeleteSessionTests {

        @Test
        @DisplayName("Should delete session as admin")
        void deleteSession_AsAdmin_ReturnsNoContent() throws Exception {
            // Given
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdSession.getId(), adminToken)
                    .andExpect(status().isNoContent());

            // Verify session is deleted (should return 404)
            performGet(BASE_URL + "/" + createdSession.getId(), adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should delete session as teacher")
        void deleteSession_AsTeacher_ReturnsNoContent() throws Exception {
            // Given
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdSession.getId(), teacherToken)
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should reject delete by student")
        void deleteSession_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When & Then
            performDelete(BASE_URL + "/" + createdSession.getId(), studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent session")
        void deleteSession_WithInvalidId_ReturnsNotFound() throws Exception {
            // When & Then
            performDelete(BASE_URL + "/99999", adminToken)
                    .andExpect(status().isNotFound());
        }
    }

    // ===========================================
    // Session Lifecycle Tests
    // ===========================================

    @Nested
    @DisplayName("Session Lifecycle Operations")
    class SessionLifecycleTests {

        @Test
        @DisplayName("Should start session (SCHEDULED -> IN_PROGRESS)")
        void startSession_FromScheduled_ReturnsInProgress() throws Exception {
            // Given - Create session
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When & Then
            performPost(BASE_URL + "/" + createdSession.getId() + "/start", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.isInProgress").value(true))
                    .andExpect(jsonPath("$.isScheduled").value(false));
        }

        @Test
        @DisplayName("Should complete session (IN_PROGRESS -> COMPLETED)")
        void completeSession_FromInProgress_ReturnsCompleted() throws Exception {
            // Given - Create and start session
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // Start the session first
            performPost(BASE_URL + "/" + createdSession.getId() + "/start", adminToken)
                    .andExpect(status().isOk());

            // When & Then - Complete the session
            performPost(BASE_URL + "/" + createdSession.getId() + "/complete", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.isCompleted").value(true))
                    .andExpect(jsonPath("$.isInProgress").value(false));
        }

        @Test
        @DisplayName("Should cancel session (SCHEDULED -> CANCELLED)")
        void cancelSession_FromScheduled_ReturnsCancelled() throws Exception {
            // Given - Create session
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When & Then
            performPost(BASE_URL + "/" + createdSession.getId() + "/cancel", adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"))
                    .andExpect(jsonPath("$.isCancelled").value(true))
                    .andExpect(jsonPath("$.isScheduled").value(false));
        }

        @Test
        @DisplayName("Should postpone session with new date")
        void postponeSession_WithNewDate_ReturnsNewSession() throws Exception {
            // Given - Create session
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);
            LocalDate newDate = LocalDate.now().plusDays(14);

            // When
            PostponeSessionRequest postponeRequest = PostponeSessionRequest.builder()
                    .newDate(newDate)
                    .build();

            // Then - Returns the newly created session with the new date
            performPost(BASE_URL + "/" + createdSession.getId() + "/postpone", postponeRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date").value(newDate.toString()))
                    .andExpect(jsonPath("$.status").value("SCHEDULED"));
        }

        @Test
        @DisplayName("Should postpone session with new date and times")
        void postponeSession_WithNewDateAndTimes_ReturnsNewSession() throws Exception {
            // Given - Create session
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);
            LocalDate newDate = LocalDate.now().plusDays(14);

            // When
            PostponeSessionRequest postponeRequest = PostponeSessionRequest.builder()
                    .newDate(newDate)
                    .newStartTime(LocalTime.of(16, 0))
                    .newEndTime(LocalTime.of(18, 0))
                    .newClassroom(Classroom.AULA_PORTAL2)
                    .newMode(SessionMode.DUAL)
                    .build();

            // Then
            performPost(BASE_URL + "/" + createdSession.getId() + "/postpone", postponeRequest, adminToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date").value(newDate.toString()))
                    .andExpect(jsonPath("$.startTime").value("16:00"))
                    .andExpect(jsonPath("$.endTime").value("18:00"))
                    .andExpect(jsonPath("$.classroom").value("AULA_PORTAL2"))
                    .andExpect(jsonPath("$.mode").value("DUAL"));
        }

        @Test
        @DisplayName("Should reject lifecycle operations by student")
        void lifecycleOperations_AsStudent_ReturnsForbidden() throws Exception {
            // Given
            String studentToken = authHelper.getStudentToken();
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When & Then - Start
            performPost(BASE_URL + "/" + createdSession.getId() + "/start", studentToken)
                    .andExpect(status().isForbidden());

            // When & Then - Cancel
            performPost(BASE_URL + "/" + createdSession.getId() + "/cancel", studentToken)
                    .andExpect(status().isForbidden());

            // When & Then - Postpone
            PostponeSessionRequest postponeRequest = PostponeSessionRequest.builder()
                    .newDate(LocalDate.now().plusDays(14))
                    .build();
            performPost(BASE_URL + "/" + createdSession.getId() + "/postpone", postponeRequest, studentToken)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for lifecycle operations on non-existent session")
        void lifecycleOperations_WithInvalidId_ReturnsNotFound() throws Exception {
            // Start
            performPost(BASE_URL + "/99999/start", adminToken)
                    .andExpect(status().isNotFound());

            // Complete
            performPost(BASE_URL + "/99999/complete", adminToken)
                    .andExpect(status().isNotFound());

            // Cancel
            performPost(BASE_URL + "/99999/cancel", adminToken)
                    .andExpect(status().isNotFound());

            // Postpone
            PostponeSessionRequest postponeRequest = PostponeSessionRequest.builder()
                    .newDate(LocalDate.now().plusDays(14))
                    .build();
            performPost(BASE_URL + "/99999/postpone", postponeRequest, adminToken)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject postpone without new date")
        void postponeSession_WithoutNewDate_ReturnsBadRequest() throws Exception {
            // Given
            CreateSessionRequest createRequest = CreateSessionRequest.builder()
                    .type(SessionType.EXTRA)
                    .groupId(testGroup.getId())
                    .classroom(Classroom.AULA_PORTAL1)
                    .date(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .mode(SessionMode.IN_PERSON)
                    .build();
            MvcResult createResult = performPost(BASE_URL, createRequest, adminToken)
                    .andExpect(status().isCreated())
                    .andReturn();

            SessionResponse createdSession = fromResponse(createResult, SessionResponse.class);

            // When
            PostponeSessionRequest postponeRequest = PostponeSessionRequest.builder()
                    .newDate(null)
                    .build();

            // Then
            performPost(BASE_URL + "/" + createdSession.getId() + "/postpone", postponeRequest, adminToken)
                    .andExpect(status().isBadRequest());
        }
    }
}
