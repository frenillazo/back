package com.acainfo.session.application.service;

import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.session.application.dto.GenerateSessionsCommand;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.InvalidSessionStateException;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import com.acainfo.shared.factory.GroupFactory;
import com.acainfo.shared.factory.ScheduleFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SessionGenerationService with Mockito.
 * Tests session generation from schedules.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionGenerationService Tests")
class SessionGenerationServiceTest {

    @Mock
    private SessionRepositoryPort sessionRepositoryPort;

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @Mock
    private ScheduleRepositoryPort scheduleRepositoryPort;

    @InjectMocks
    private SessionGenerationService sessionGenerationService;

    @Captor
    private ArgumentCaptor<List<Session>> sessionsCaptor;

    private SubjectGroup testGroup;
    private Schedule mondaySchedule;
    private Schedule wednesdaySchedule;

    @BeforeEach
    void setUp() {
        testGroup = GroupFactory.defaultGroup();

        mondaySchedule = ScheduleFactory.builder()
                .id(1L)
                .groupId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .classroom(Classroom.AULA_PORTAL1)
                .buildDomain();

        wednesdaySchedule = ScheduleFactory.builder()
                .id(2L)
                .groupId(1L)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .classroom(Classroom.AULA_PORTAL2)
                .buildDomain();
    }

    @Nested
    @DisplayName("Generate Sessions Tests")
    class GenerateSessionsTests {

        @Test
        @DisplayName("Should generate sessions for a week with Monday schedule")
        void generate_WithMondaySchedule_GeneratesCorrectSessions() {
            // Given - A week starting on Sunday, Jan 5, 2025 to Saturday, Jan 11, 2025
            // Monday Jan 6 is the only Monday in this range
            LocalDate startDate = LocalDate.of(2025, 1, 5); // Sunday
            LocalDate endDate = LocalDate.of(2025, 1, 11);   // Saturday

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, startDate, endDate
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(mondaySchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.existsByScheduleIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
            when(sessionRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> {
                List<Session> sessions = invocation.getArgument(0);
                return sessions.stream()
                        .map(s -> s.toBuilder().id((long) (sessions.indexOf(s) + 1)).build())
                        .toList();
            });

            // When
            List<Session> result = sessionGenerationService.generate(command);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2025, 1, 6)); // Monday
            assertThat(result.get(0).getScheduleId()).isEqualTo(1L);
            assertThat(result.get(0).getType()).isEqualTo(SessionType.REGULAR);
            assertThat(result.get(0).getStatus()).isEqualTo(SessionStatus.SCHEDULED);

            verify(sessionRepositoryPort).saveAll(anyList());
        }

        @Test
        @DisplayName("Should generate sessions for multiple schedules")
        void generate_WithMultipleSchedules_GeneratesAllSessions() {
            // Given - Two weeks to include multiple Mondays and Wednesdays
            LocalDate startDate = LocalDate.of(2025, 1, 6);  // Monday
            LocalDate endDate = LocalDate.of(2025, 1, 19);   // Sunday (includes 2 Mondays, 2 Wednesdays)

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, startDate, endDate
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(mondaySchedule, wednesdaySchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.existsByScheduleIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
            when(sessionRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            List<Session> result = sessionGenerationService.generate(command);

            // Then - 2 Mondays (6, 13) + 2 Wednesdays (8, 15) = 4 sessions
            assertThat(result).hasSize(4);
            verify(sessionRepositoryPort).saveAll(sessionsCaptor.capture());

            List<Session> savedSessions = sessionsCaptor.getValue();
            assertThat(savedSessions).extracting(Session::getScheduleId)
                    .containsExactlyInAnyOrder(1L, 1L, 2L, 2L);
        }

        @Test
        @DisplayName("Should skip dates where session already exists")
        void generate_WithExistingSession_SkipsThatDate() {
            // Given
            LocalDate startDate = LocalDate.of(2025, 1, 6);  // Monday
            LocalDate endDate = LocalDate.of(2025, 1, 12);   // Sunday

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, startDate, endDate
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(mondaySchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            // Session already exists for Monday Jan 6
            when(sessionRepositoryPort.existsByScheduleIdAndDate(1L, LocalDate.of(2025, 1, 6))).thenReturn(true);
            when(sessionRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            List<Session> result = sessionGenerationService.generate(command);

            // Then
            assertThat(result).isEmpty();
            verify(sessionRepositoryPort).saveAll(Collections.emptyList());
        }

        @Test
        @DisplayName("Should return empty list when no schedules for group")
        void generate_WithNoSchedules_ReturnsEmptyList() {
            // Given
            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L,
                    LocalDate.of(2025, 1, 6),
                    LocalDate.of(2025, 1, 12)
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(Collections.emptyList());

            // When
            List<Session> result = sessionGenerationService.generate(command);

            // Then
            assertThat(result).isEmpty();
            verify(sessionRepositoryPort, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Should throw exception when groupId is null")
        void generate_WithNullGroupId_ThrowsException() {
            // Given
            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    null, // No groupId
                    LocalDate.of(2025, 1, 6),
                    LocalDate.of(2025, 1, 12)
            );

            // When & Then
            assertThatThrownBy(() -> sessionGenerationService.generate(command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("groupId");

            verify(sessionRepositoryPort, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Should throw exception when group not found")
        void generate_WithNonExistentGroup_ThrowsException() {
            // Given
            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    999L,
                    LocalDate.of(2025, 1, 6),
                    LocalDate.of(2025, 1, 12)
            );

            when(scheduleRepositoryPort.findByGroupId(999L)).thenReturn(List.of(mondaySchedule));
            when(groupRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> sessionGenerationService.generate(command))
                    .isInstanceOf(GroupNotFoundException.class);

            verify(sessionRepositoryPort, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("Should generate sessions with correct properties from schedule")
        void generate_SetsCorrectPropertiesFromSchedule() {
            // Given
            LocalDate startDate = LocalDate.of(2025, 1, 6); // Monday
            LocalDate endDate = LocalDate.of(2025, 1, 6);   // Same day

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, startDate, endDate
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(mondaySchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.existsByScheduleIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
            when(sessionRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            List<Session> result = sessionGenerationService.generate(command);

            // Then
            assertThat(result).hasSize(1);
            Session session = result.get(0);

            assertThat(session.getSubjectId()).isEqualTo(testGroup.getSubjectId());
            assertThat(session.getGroupId()).isEqualTo(1L);
            assertThat(session.getScheduleId()).isEqualTo(mondaySchedule.getId());
            assertThat(session.getClassroom()).isEqualTo(mondaySchedule.getClassroom());
            assertThat(session.getStartTime()).isEqualTo(mondaySchedule.getStartTime());
            assertThat(session.getEndTime()).isEqualTo(mondaySchedule.getEndTime());
            assertThat(session.getType()).isEqualTo(SessionType.REGULAR);
            assertThat(session.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
        }
    }

    @Nested
    @DisplayName("Preview Sessions Tests")
    class PreviewSessionsTests {

        @Test
        @DisplayName("Should preview sessions without saving")
        void preview_DoesNotSaveSessions() {
            // Given
            LocalDate startDate = LocalDate.of(2025, 1, 6);
            LocalDate endDate = LocalDate.of(2025, 1, 12);

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, startDate, endDate
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(mondaySchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.existsByScheduleIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);

            // When
            List<Session> result = sessionGenerationService.preview(command);

            // Then
            assertThat(result).hasSize(1);
            verify(sessionRepositoryPort, never()).saveAll(anyList());
            verify(sessionRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should return same results as generate would")
        void preview_ReturnsSameResultsAsGenerate() {
            // Given
            LocalDate startDate = LocalDate.of(2025, 1, 6);
            LocalDate endDate = LocalDate.of(2025, 1, 19);

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, startDate, endDate
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(mondaySchedule, wednesdaySchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.existsByScheduleIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);

            // When
            List<Session> previewResult = sessionGenerationService.preview(command);

            // Then - Same count as would be generated
            assertThat(previewResult).hasSize(4);

            // Verify sessions have no IDs (not persisted)
            assertThat(previewResult).allMatch(s -> s.getId() == null);
        }

        @Test
        @DisplayName("Should throw exception when groupId is null for preview")
        void preview_WithNullGroupId_ThrowsException() {
            // Given
            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    null,
                    LocalDate.of(2025, 1, 6),
                    LocalDate.of(2025, 1, 12)
            );

            // When & Then
            assertThatThrownBy(() -> sessionGenerationService.preview(command))
                    .isInstanceOf(InvalidSessionStateException.class)
                    .hasMessageContaining("groupId");
        }

        @Test
        @DisplayName("Should return empty list when no schedules for preview")
        void preview_WithNoSchedules_ReturnsEmptyList() {
            // Given
            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L,
                    LocalDate.of(2025, 1, 6),
                    LocalDate.of(2025, 1, 12)
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(Collections.emptyList());

            // When
            List<Session> result = sessionGenerationService.preview(command);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Session Mode Determination Tests")
    class SessionModeDeterminationTests {

        @Test
        @DisplayName("Should set IN_PERSON mode for physical classroom")
        void generate_WithPhysicalClassroom_SetsInPersonMode() {
            // Given
            Schedule physicalSchedule = ScheduleFactory.builder()
                    .id(1L)
                    .groupId(1L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .classroom(Classroom.AULA_PORTAL1) // Physical
                    .buildDomain();

            LocalDate startDate = LocalDate.of(2025, 1, 6);

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, startDate, startDate
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(physicalSchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.existsByScheduleIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
            when(sessionRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            List<Session> result = sessionGenerationService.generate(command);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMode()).isEqualTo(SessionMode.IN_PERSON);
        }

        @Test
        @DisplayName("Should set ONLINE mode for virtual classroom")
        void generate_WithVirtualClassroom_SetsOnlineMode() {
            // Given
            Schedule virtualSchedule = ScheduleFactory.builder()
                    .id(1L)
                    .groupId(1L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .classroom(Classroom.AULA_VIRTUAL) // Virtual
                    .buildDomain();

            LocalDate startDate = LocalDate.of(2025, 1, 6);

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, startDate, startDate
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(virtualSchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.existsByScheduleIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
            when(sessionRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            List<Session> result = sessionGenerationService.generate(command);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMode()).isEqualTo(SessionMode.ONLINE);
        }
    }

    @Nested
    @DisplayName("Date Range Tests")
    class DateRangeTests {

        @Test
        @DisplayName("Should handle single day range")
        void generate_WithSingleDayRange_GeneratesForThatDay() {
            // Given - Single Monday
            LocalDate singleDay = LocalDate.of(2025, 1, 6); // Monday

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, singleDay, singleDay
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(mondaySchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.existsByScheduleIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
            when(sessionRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            List<Session> result = sessionGenerationService.generate(command);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDate()).isEqualTo(singleDay);
        }

        @Test
        @DisplayName("Should return empty when no matching days in range")
        void generate_WithNoMatchingDays_ReturnsEmpty() {
            // Given - Tuesday to Thursday (no Monday)
            LocalDate startDate = LocalDate.of(2025, 1, 7);  // Tuesday
            LocalDate endDate = LocalDate.of(2025, 1, 9);    // Thursday

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, startDate, endDate
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(mondaySchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));

            // When
            List<Session> result = sessionGenerationService.generate(command);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should generate for multiple weeks")
        void generate_WithMultipleWeeks_GeneratesAllSessions() {
            // Given - 4 weeks (should have 4 Mondays)
            LocalDate startDate = LocalDate.of(2025, 1, 6);   // Monday
            LocalDate endDate = LocalDate.of(2025, 2, 2);     // Sunday (4 full weeks)

            GenerateSessionsCommand command = new GenerateSessionsCommand(
                    1L, startDate, endDate
            );

            when(scheduleRepositoryPort.findByGroupId(1L)).thenReturn(List.of(mondaySchedule));
            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(sessionRepositoryPort.existsByScheduleIdAndDate(anyLong(), any(LocalDate.class))).thenReturn(false);
            when(sessionRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            List<Session> result = sessionGenerationService.generate(command);

            // Then - 4 Mondays: Jan 6, 13, 20, 27
            assertThat(result).hasSize(4);
            assertThat(result).extracting(Session::getDate)
                    .containsExactly(
                            LocalDate.of(2025, 1, 6),
                            LocalDate.of(2025, 1, 13),
                            LocalDate.of(2025, 1, 20),
                            LocalDate.of(2025, 1, 27)
                    );
        }
    }
}
