package com.acainfo.session.application.service;

import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.exception.CourseNotFoundException;
import com.acainfo.course.domain.model.CourseStatus;
import com.acainfo.course.domain.model.Course;
import com.acainfo.reservation.application.dto.GenerateReservationsCommand;
import com.acainfo.reservation.application.port.in.GenerateReservationsUseCase;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.session.application.dto.GenerateSessionsCommand;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.InvalidSessionStateException;
import com.acainfo.session.domain.exception.TeacherSessionConflictException;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link SessionGenerationService}.
 * Captures the CURRENT behavior of REGULAR session generation from schedules
 * before the group -> course migration.
 */
@ExtendWith(MockitoExtension.class)
class SessionGenerationServiceTest {

    private static final Long GROUP_ID = 10L;
    private static final Long SUBJECT_ID = 100L;
    private static final Long OTHER_SUBJECT_ID = 101L;
    private static final Long TEACHER_ID = 200L;
    private static final Long SCHEDULE_ID = 1L;
    private static final Long SECOND_SCHEDULE_ID = 2L;

    // July 2026: the 6th and the 13th are Mondays; the 19th is a Sunday.
    private static final LocalDate MONDAY_1 = LocalDate.of(2026, 7, 6);
    private static final LocalDate MONDAY_2 = LocalDate.of(2026, 7, 13);
    private static final LocalDate RANGE_START = LocalDate.of(2026, 7, 6);
    private static final LocalDate RANGE_END = LocalDate.of(2026, 7, 19);

    private static final LocalTime TEN = LocalTime.of(10, 0);
    private static final LocalTime ELEVEN = LocalTime.of(11, 0);
    private static final LocalTime TWELVE = LocalTime.of(12, 0);
    private static final LocalTime THIRTEEN = LocalTime.of(13, 0);
    private static final LocalTime FOURTEEN = LocalTime.of(14, 0);

    @Mock
    private SessionRepositoryPort sessionRepositoryPort;

    @Mock
    private CourseRepositoryPort courseRepositoryPort;

    @Mock
    private ScheduleRepositoryPort scheduleRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private GenerateReservationsUseCase generateReservationsUseCase;

    @InjectMocks
    private SessionGenerationService service;

    // ==================== Fixtures ====================

    private Course group(LocalDate startDate, LocalDate endDate) {
        return Course.builder()
                .id(GROUP_ID)
                .name("Programación grupo 1 25-26")
                .subjectId(SUBJECT_ID)
                .teacherId(TEACHER_ID)
                .status(CourseStatus.OPEN)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private Schedule mondaySchedule(Long id, LocalTime start, LocalTime end, Classroom classroom) {
        return Schedule.builder()
                .id(id)
                .courseId(GROUP_ID)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(start)
                .endTime(end)
                .classroom(classroom)
                .build();
    }

    private Session existingTeacherSession(Long subjectId, LocalTime start, LocalTime end, SessionMode mode) {
        return Session.builder()
                .id(500L)
                .subjectId(subjectId)
                .courseId(99L)
                .date(MONDAY_1)
                .startTime(start)
                .endTime(end)
                .status(SessionStatus.SCHEDULED)
                .type(SessionType.REGULAR)
                .mode(mode)
                .build();
    }

    private User teacher() {
        return User.builder()
                .id(TEACHER_ID)
                .email("ana.garcia@acainfo.com")
                .firstName("Ana")
                .lastName("García")
                .build();
    }

    /** Stubs saveAll to echo back the sessions with sequential IDs starting at 1000. */
    private void stubSaveAllAssigningIds() {
        when(sessionRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> {
            List<Session> toSave = invocation.getArgument(0);
            List<Session> saved = new ArrayList<>();
            long id = 1000L;
            for (Session s : toSave) {
                saved.add(s.toBuilder().id(id++).build());
            }
            return saved;
        });
    }

    // ==================== Input validation / lookups ====================

    @Test
    void shouldThrowInvalidSessionStateWhenCourseIdIsNull() {
        GenerateSessionsCommand command = GenerateSessionsCommand.forAllGroups(RANGE_START, RANGE_END);

        assertThatThrownBy(() -> service.generate(command))
                .isInstanceOf(InvalidSessionStateException.class)
                .hasMessageContaining("Generation for all groups not yet implemented");

        verifyNoInteractions(scheduleRepositoryPort, courseRepositoryPort,
                sessionRepositoryPort, generateReservationsUseCase);
    }

    @Test
    void shouldReturnEmptyListWithoutCheckingGroupWhenGroupHasNoSchedules() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID)).thenReturn(List.of());

        List<Session> result = service.generate(
                GenerateSessionsCommand.forCourse(GROUP_ID, RANGE_START, RANGE_END));

        assertThat(result).isEmpty();
        // Current behavior: group existence is NOT verified when there are no schedules
        // (a nonexistent courseId returns an empty list instead of CourseNotFoundException).
        verifyNoInteractions(courseRepositoryPort);
        verify(sessionRepositoryPort, never()).saveAll(anyList());
        verifyNoInteractions(generateReservationsUseCase);
    }

    @Test
    void shouldThrowGroupNotFoundWhenSchedulesExistButGroupDoesNot() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(
                GenerateSessionsCommand.forCourse(GROUP_ID, RANGE_START, RANGE_END)))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining("Course not found with ID: " + GROUP_ID);

        verify(sessionRepositoryPort, never()).saveAll(anyList());
        verifyNoInteractions(generateReservationsUseCase);
    }

    // ==================== Happy path: generation from schedules ====================

    @Test
    void shouldGenerateRegularSessionsOnMatchingWeekdaysWithinRange() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(eq(SCHEDULE_ID), any(LocalDate.class)))
                .thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(eq(TEACHER_ID), any(LocalDate.class)))
                .thenReturn(List.of());
        stubSaveAllAssigningIds();

        List<Session> result = service.generate(
                GenerateSessionsCommand.forCourse(GROUP_ID, RANGE_START, RANGE_END));

        // Two Mondays in the range -> two sessions, on the correct dates only
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Session::getDate).containsExactly(MONDAY_1, MONDAY_2);
        assertThat(result).allSatisfy(session -> {
            assertThat(session.getSubjectId()).isEqualTo(SUBJECT_ID);
            assertThat(session.getCourseId()).isEqualTo(GROUP_ID);
            assertThat(session.getScheduleId()).isEqualTo(SCHEDULE_ID);
            assertThat(session.getClassroom()).isEqualTo(Classroom.AULA_PORTAL1);
            assertThat(session.getStartTime()).isEqualTo(TEN);
            assertThat(session.getEndTime()).isEqualTo(TWELVE);
            assertThat(session.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
            assertThat(session.getType()).isEqualTo(SessionType.REGULAR);
            assertThat(session.getMode()).isEqualTo(SessionMode.IN_PERSON);
        });
        verify(sessionRepositoryPort).saveAll(anyList());
    }

    @Test
    void shouldMapVirtualClassroomToOnlineMode() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_VIRTUAL)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1)).thenReturn(List.of());

        List<Session> result = service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMode()).isEqualTo(SessionMode.ONLINE);
    }

    @Test
    void shouldMapNullClassroomToDualMode() {
        // Current behavior quirk: a schedule with no classroom is neither physical nor
        // online, so the generated session falls through to DUAL mode with null classroom.
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, null)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1)).thenReturn(List.of());

        List<Session> result = service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMode()).isEqualTo(SessionMode.DUAL);
        assertThat(result.get(0).getClassroom()).isNull();
    }

    // ==================== Idempotency ====================

    @Test
    void shouldSkipDatesWhereSessionAlreadyExistsForScheduleAndDate() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(true);
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_2)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_2)).thenReturn(List.of());
        stubSaveAllAssigningIds();

        List<Session> result = service.generate(
                GenerateSessionsCommand.forCourse(GROUP_ID, RANGE_START, RANGE_END));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(MONDAY_2);
        // No teacher-conflict check is even attempted for the already-existing date
        verify(sessionRepositoryPort, never()).findByTeacherIdAndDate(TEACHER_ID, MONDAY_1);
    }

    // ==================== Group endDate capping ====================

    @Test
    void shouldCapGenerationAtGroupEndDateWhenBeforeCommandEndDate() {
        // Group ends Wednesday 2026-07-08: only the first Monday is generated
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, LocalDate.of(2026, 7, 8))));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1)).thenReturn(List.of());

        List<Session> result = service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, RANGE_START, RANGE_END));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(MONDAY_1);
        verify(sessionRepositoryPort, never()).existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_2);
    }

    @Test
    void shouldGenerateSessionOnGroupEndDateWhenItMatchesSchedule() {
        // endDate is inclusive: a session IS generated on the group's last day
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, MONDAY_2)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(eq(SCHEDULE_ID), any(LocalDate.class)))
                .thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(eq(TEACHER_ID), any(LocalDate.class)))
                .thenReturn(List.of());

        List<Session> result = service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, RANGE_START, RANGE_END));

        assertThat(result).extracting(Session::getDate).containsExactly(MONDAY_1, MONDAY_2);
    }

    @Test
    void shouldUseCommandEndDateWhenGroupEndDateIsNull() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, null)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(eq(SCHEDULE_ID), any(LocalDate.class)))
                .thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(eq(TEACHER_ID), any(LocalDate.class)))
                .thenReturn(List.of());

        List<Session> result = service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, RANGE_START, RANGE_END));

        assertThat(result).extracting(Session::getDate).containsExactly(MONDAY_1, MONDAY_2);
    }

    @Test
    void shouldGenerateSessionsBeforeGroupStartDate() {
        // Current behavior quirk: only the group's endDate caps the range.
        // The group's startDate is IGNORED, so sessions can be generated before
        // the group officially starts if the caller passes an earlier startDate.
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(LocalDate.of(2026, 7, 10), RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(eq(SCHEDULE_ID), any(LocalDate.class)))
                .thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(eq(TEACHER_ID), any(LocalDate.class)))
                .thenReturn(List.of());

        List<Session> result = service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, RANGE_START, RANGE_END));

        // MONDAY_1 (2026-07-06) is before the group's startDate (2026-07-10) but is generated anyway
        assertThat(result).extracting(Session::getDate).containsExactly(MONDAY_1, MONDAY_2);
    }

    // ==================== Teacher conflicts vs existing sessions ====================

    @Test
    void shouldThrowTeacherConflictWhenOverlappingExistingSessionIsNotOnline() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1))
                .thenReturn(List.of(existingTeacherSession(SUBJECT_ID, ELEVEN, THIRTEEN, SessionMode.IN_PERSON)));
        when(userRepositoryPort.findById(TEACHER_ID)).thenReturn(Optional.of(teacher()));

        assertThatThrownBy(() -> service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1)))
                .isInstanceOf(TeacherSessionConflictException.class)
                .hasMessageContaining("Ana García")
                .hasMessageContaining("06/07/2026")
                .hasMessageContaining("11:00")
                .hasMessageContaining("13:00");
    }

    @Test
    void shouldUseTeacherIdFallbackInConflictMessageWhenTeacherUserNotFound() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1))
                .thenReturn(List.of(existingTeacherSession(SUBJECT_ID, ELEVEN, THIRTEEN, SessionMode.IN_PERSON)));
        when(userRepositoryPort.findById(TEACHER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1)))
                .isInstanceOf(TeacherSessionConflictException.class)
                .hasMessageContaining("ID " + TEACHER_ID);
    }

    @Test
    void shouldAllowOverlapWhenBothSessionsAreOnlineAndSameSubject() {
        // New session from AULA_VIRTUAL schedule -> ONLINE; existing overlapping session
        // is ONLINE and same subject -> the only allowed overlap combination.
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_VIRTUAL)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1))
                .thenReturn(List.of(existingTeacherSession(SUBJECT_ID, ELEVEN, THIRTEEN, SessionMode.ONLINE)));

        List<Session> result = service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMode()).isEqualTo(SessionMode.ONLINE);
        verifyNoInteractions(userRepositoryPort);
    }

    @Test
    void shouldThrowTeacherConflictWhenBothOnlineButDifferentSubject() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_VIRTUAL)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1))
                .thenReturn(List.of(existingTeacherSession(OTHER_SUBJECT_ID, ELEVEN, THIRTEEN, SessionMode.ONLINE)));
        when(userRepositoryPort.findById(TEACHER_ID)).thenReturn(Optional.of(teacher()));

        assertThatThrownBy(() -> service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1)))
                .isInstanceOf(TeacherSessionConflictException.class);
    }

    @Test
    void shouldThrowTeacherConflictWhenSameSubjectButNotBothOnline() {
        // New session is ONLINE but the existing one is IN_PERSON: same subject is not enough
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_VIRTUAL)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1))
                .thenReturn(List.of(existingTeacherSession(SUBJECT_ID, ELEVEN, THIRTEEN, SessionMode.IN_PERSON)));
        when(userRepositoryPort.findById(TEACHER_ID)).thenReturn(Optional.of(teacher()));

        assertThatThrownBy(() -> service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1)))
                .isInstanceOf(TeacherSessionConflictException.class);
    }

    @Test
    void shouldNotConflictWhenSessionsAreBackToBack() {
        // Existing session 12:00-14:00 vs new 10:00-12:00: shared boundary is NOT an overlap
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1))
                .thenReturn(List.of(existingTeacherSession(OTHER_SUBJECT_ID, TWELVE, FOURTEEN, SessionMode.IN_PERSON)));

        List<Session> result = service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1));

        assertThat(result).hasSize(1);
        verifyNoInteractions(userRepositoryPort);
    }

    // ==================== Teacher conflicts within the generated batch ====================

    @Test
    void shouldThrowTeacherConflictWhenTwoSchedulesInBatchOverlapOnSameDay() {
        // Two physical schedules of the same group overlap on Monday (10-12 and 11-13):
        // the second candidate session conflicts with the first one of the same batch.
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(
                        mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1),
                        mondaySchedule(SECOND_SCHEDULE_ID, ELEVEN, THIRTEEN, Classroom.AULA_PORTAL2)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(any(Long.class), eq(MONDAY_1))).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1)).thenReturn(List.of());
        when(userRepositoryPort.findById(TEACHER_ID)).thenReturn(Optional.of(teacher()));

        assertThatThrownBy(() -> service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1)))
                .isInstanceOf(TeacherSessionConflictException.class)
                .hasMessageContaining("Ana García")
                // The reported times are those of the batch session already accepted (10-12)
                .hasMessageContaining("10:00")
                .hasMessageContaining("12:00");
    }

    @Test
    void shouldAllowBatchOverlapWhenBothSchedulesAreOnlineAndSameSubject() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(
                        mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_VIRTUAL),
                        mondaySchedule(SECOND_SCHEDULE_ID, ELEVEN, THIRTEEN, Classroom.AULA_VIRTUAL)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(any(Long.class), eq(MONDAY_1))).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1)).thenReturn(List.of());

        List<Session> result = service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1));

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getMode() == SessionMode.ONLINE);
        verifyNoInteractions(userRepositoryPort);
    }

    @Test
    void shouldNotSaveNorGenerateReservationsWhenConflictDetectedDuringGenerate() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(SCHEDULE_ID, MONDAY_1)).thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(TEACHER_ID, MONDAY_1))
                .thenReturn(List.of(existingTeacherSession(SUBJECT_ID, ELEVEN, THIRTEEN, SessionMode.IN_PERSON)));
        when(userRepositoryPort.findById(TEACHER_ID)).thenReturn(Optional.of(teacher()));

        assertThatThrownBy(() -> service.generate(
                GenerateSessionsCommand.forCourse(GROUP_ID, MONDAY_1, MONDAY_1)))
                .isInstanceOf(TeacherSessionConflictException.class);

        // The whole generation aborts: nothing is saved, no reservations triggered
        verify(sessionRepositoryPort, never()).saveAll(anyList());
        verifyNoInteractions(generateReservationsUseCase);
    }

    // ==================== Reservation triggering ====================

    @Test
    void shouldTriggerReservationGenerationForEachCreatedSession() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(eq(SCHEDULE_ID), any(LocalDate.class)))
                .thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(eq(TEACHER_ID), any(LocalDate.class)))
                .thenReturn(List.of());
        stubSaveAllAssigningIds();

        List<Session> result = service.generate(
                GenerateSessionsCommand.forCourse(GROUP_ID, RANGE_START, RANGE_END));

        assertThat(result).hasSize(2);

        ArgumentCaptor<GenerateReservationsCommand> captor =
                ArgumentCaptor.forClass(GenerateReservationsCommand.class);
        verify(generateReservationsUseCase, times(2)).generate(captor.capture());

        List<GenerateReservationsCommand> commands = captor.getAllValues();
        assertThat(commands).containsExactly(
                new GenerateReservationsCommand(1000L, GROUP_ID),
                new GenerateReservationsCommand(1001L, GROUP_ID));
    }

    @Test
    void shouldNotSaveOrTriggerReservationsOnPreview() {
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));
        when(sessionRepositoryPort.existsByScheduleIdAndDate(eq(SCHEDULE_ID), any(LocalDate.class)))
                .thenReturn(false);
        when(sessionRepositoryPort.findByTeacherIdAndDate(eq(TEACHER_ID), any(LocalDate.class)))
                .thenReturn(List.of());

        List<Session> result = service.preview(
                GenerateSessionsCommand.forCourse(GROUP_ID, RANGE_START, RANGE_END));

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(session -> assertThat(session.getId()).isNull());
        verify(sessionRepositoryPort, never()).saveAll(anyList());
        verify(sessionRepositoryPort, never()).save(any(Session.class));
        verifyNoInteractions(generateReservationsUseCase);
    }

    @Test
    void shouldReturnEmptyAndSkipSavingWhenNoDateMatchesAnySchedule() {
        // Range covers only Tue-Sun (2026-07-07 .. 2026-07-12): no Monday inside
        when(scheduleRepositoryPort.findByCourseId(GROUP_ID))
                .thenReturn(List.of(mondaySchedule(SCHEDULE_ID, TEN, TWELVE, Classroom.AULA_PORTAL1)));
        when(courseRepositoryPort.findById(GROUP_ID))
                .thenReturn(Optional.of(group(RANGE_START, RANGE_END)));

        List<Session> result = service.generate(GenerateSessionsCommand.forCourse(
                GROUP_ID, LocalDate.of(2026, 7, 7), LocalDate.of(2026, 7, 12)));

        assertThat(result).isEmpty();
        verify(sessionRepositoryPort, never()).saveAll(anyList());
        verifyNoInteractions(generateReservationsUseCase);
    }
}
