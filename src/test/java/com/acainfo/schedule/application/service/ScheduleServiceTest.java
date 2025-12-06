package com.acainfo.schedule.application.service;

import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.application.dto.CreateScheduleCommand;
import com.acainfo.schedule.application.dto.ScheduleFilters;
import com.acainfo.schedule.application.dto.UpdateScheduleCommand;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.exception.InvalidScheduleDataException;
import com.acainfo.schedule.domain.exception.ScheduleConflictException;
import com.acainfo.schedule.domain.exception.ScheduleNotFoundException;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.shared.factory.GroupFactory;
import com.acainfo.shared.factory.ScheduleFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ScheduleService with Mockito.
 * Tests business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService Tests")
class ScheduleServiceTest {

    @Mock
    private ScheduleRepositoryPort scheduleRepositoryPort;

    @Mock
    private GroupRepositoryPort groupRepositoryPort;

    @InjectMocks
    private ScheduleService scheduleService;

    private Schedule testSchedule;
    private SubjectGroup testGroup;

    @BeforeEach
    void setUp() {
        testSchedule = ScheduleFactory.defaultSchedule();
        testGroup = GroupFactory.defaultGroup();
    }

    @Nested
    @DisplayName("Create Schedule Tests")
    class CreateScheduleTests {

        @Test
        @DisplayName("Should create schedule successfully with valid data")
        void create_WithValidData_CreatesSchedule() {
            // Given
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L, // groupId
                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    Classroom.AULA_PORTAL1
            );

            Page<Schedule> emptyPage = new PageImpl<>(Collections.emptyList());

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(emptyPage);
            when(scheduleRepositoryPort.save(any(Schedule.class))).thenReturn(testSchedule);

            // When
            Schedule result = scheduleService.create(command);

            // Then
            assertThat(result).isNotNull();
            verify(groupRepositoryPort).findById(1L);
            verify(scheduleRepositoryPort).save(any(Schedule.class));
        }

        @Test
        @DisplayName("Should throw exception when group not found")
        void create_WhenGroupNotFound_ThrowsException() {
            // Given
            CreateScheduleCommand command = new CreateScheduleCommand(
                    999L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    Classroom.AULA_PORTAL1
            );

            when(groupRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> scheduleService.create(command))
                    .isInstanceOf(GroupNotFoundException.class);

            verify(scheduleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when start time is after end time")
        void create_WhenStartTimeAfterEndTime_ThrowsException() {
            // Given
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(11, 0), // Start after end
                    LocalTime.of(9, 0),
                    Classroom.AULA_PORTAL1
            );

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));

            // When & Then
            assertThatThrownBy(() -> scheduleService.create(command))
                    .isInstanceOf(InvalidScheduleDataException.class)
                    .hasMessageContaining("Start time must be before end time");

            verify(scheduleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when start time equals end time")
        void create_WhenStartTimeEqualsEndTime_ThrowsException() {
            // Given
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    LocalTime.of(9, 0), // Same as start
                    Classroom.AULA_PORTAL1
            );

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));

            // When & Then
            assertThatThrownBy(() -> scheduleService.create(command))
                    .isInstanceOf(InvalidScheduleDataException.class)
                    .hasMessageContaining("Start time must be before end time");

            verify(scheduleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when schedule conflicts with existing")
        void create_WhenConflictExists_ThrowsException() {
            // Given
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    Classroom.AULA_PORTAL1
            );

            Schedule existingSchedule = ScheduleFactory.builder()
                    .id(2L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(12, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .buildDomain();

            Page<Schedule> pageWithConflict = new PageImpl<>(List.of(existingSchedule));

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(pageWithConflict);

            // When & Then
            assertThatThrownBy(() -> scheduleService.create(command))
                    .isInstanceOf(ScheduleConflictException.class);

            verify(scheduleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should allow schedule when no time overlap")
        void create_WhenNoTimeOverlap_CreatesSchedule() {
            // Given
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(14, 0),
                    LocalTime.of(16, 0),
                    Classroom.AULA_PORTAL1
            );

            // Existing schedule is 9-11, new one is 14-16 (no overlap)
            Schedule existingSchedule = ScheduleFactory.builder()
                    .id(2L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .buildDomain();

            Page<Schedule> pageWithNoConflict = new PageImpl<>(List.of(existingSchedule));

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(pageWithNoConflict);
            when(scheduleRepositoryPort.save(any(Schedule.class))).thenAnswer(invocation -> {
                Schedule s = invocation.getArgument(0);
                return s.toBuilder().id(3L).build();
            });

            // When
            Schedule result = scheduleService.create(command);

            // Then
            assertThat(result).isNotNull();
            verify(scheduleRepositoryPort).save(any(Schedule.class));
        }

        @Test
        @DisplayName("Should allow schedule in different classroom")
        void create_WhenDifferentClassroom_CreatesSchedule() {
            // Given
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    Classroom.AULA_PORTAL2 // Different classroom
            );

            Page<Schedule> emptyPage = new PageImpl<>(Collections.emptyList());

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(emptyPage);
            when(scheduleRepositoryPort.save(any(Schedule.class))).thenReturn(testSchedule);

            // When
            Schedule result = scheduleService.create(command);

            // Then
            assertThat(result).isNotNull();
            verify(scheduleRepositoryPort).save(any(Schedule.class));
        }

        @Test
        @DisplayName("Should allow schedule on different day")
        void create_WhenDifferentDay_CreatesSchedule() {
            // Given
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.TUESDAY, // Different day
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    Classroom.AULA_PORTAL1
            );

            Page<Schedule> emptyPage = new PageImpl<>(Collections.emptyList());

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(emptyPage);
            when(scheduleRepositoryPort.save(any(Schedule.class))).thenReturn(testSchedule);

            // When
            Schedule result = scheduleService.create(command);

            // Then
            assertThat(result).isNotNull();
            verify(scheduleRepositoryPort).save(any(Schedule.class));
        }
    }

    @Nested
    @DisplayName("Update Schedule Tests")
    class UpdateScheduleTests {

        @Test
        @DisplayName("Should update schedule successfully")
        void update_WithValidData_UpdatesSchedule() {
            // Given
            Long scheduleId = 1L;
            UpdateScheduleCommand command = new UpdateScheduleCommand(
                    DayOfWeek.WEDNESDAY,
                    LocalTime.of(14, 0),
                    LocalTime.of(16, 0),
                    null // Keep existing classroom
            );

            Page<Schedule> emptyPage = new PageImpl<>(Collections.emptyList());

            when(scheduleRepositoryPort.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(emptyPage);
            when(scheduleRepositoryPort.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Schedule result = scheduleService.update(scheduleId, command);

            // Then
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(14, 0));
            assertThat(result.getEndTime()).isEqualTo(LocalTime.of(16, 0));
            verify(scheduleRepositoryPort).save(any(Schedule.class));
        }

        @Test
        @DisplayName("Should skip null fields during update")
        void update_WithNullFields_SkipsNullFields() {
            // Given
            Long scheduleId = 1L;
            UpdateScheduleCommand command = new UpdateScheduleCommand(null, null, null, null);

            Page<Schedule> emptyPage = new PageImpl<>(Collections.emptyList());

            when(scheduleRepositoryPort.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(emptyPage);
            when(scheduleRepositoryPort.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Schedule result = scheduleService.update(scheduleId, command);

            // Then
            assertThat(result.getDayOfWeek()).isEqualTo(testSchedule.getDayOfWeek());
            assertThat(result.getStartTime()).isEqualTo(testSchedule.getStartTime());
            assertThat(result.getEndTime()).isEqualTo(testSchedule.getEndTime());
            assertThat(result.getClassroom()).isEqualTo(testSchedule.getClassroom());
        }

        @Test
        @DisplayName("Should throw exception when schedule not found")
        void update_WhenScheduleNotFound_ThrowsException() {
            // Given
            Long scheduleId = 999L;
            UpdateScheduleCommand command = new UpdateScheduleCommand(
                    DayOfWeek.WEDNESDAY, null, null, null
            );

            when(scheduleRepositoryPort.findById(scheduleId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> scheduleService.update(scheduleId, command))
                    .isInstanceOf(ScheduleNotFoundException.class);

            verify(scheduleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when update creates conflict")
        void update_WhenCreatesConflict_ThrowsException() {
            // Given
            Long scheduleId = 1L;
            UpdateScheduleCommand command = new UpdateScheduleCommand(
                    DayOfWeek.MONDAY,
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0),
                    Classroom.AULA_PORTAL1
            );

            Schedule existingConflict = ScheduleFactory.builder()
                    .id(2L) // Different ID
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(11, 0))
                    .endTime(LocalTime.of(13, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .buildDomain();

            Page<Schedule> pageWithConflict = new PageImpl<>(List.of(existingConflict));

            when(scheduleRepositoryPort.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(pageWithConflict);

            // When & Then
            assertThatThrownBy(() -> scheduleService.update(scheduleId, command))
                    .isInstanceOf(ScheduleConflictException.class);

            verify(scheduleRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should exclude self when checking conflicts during update")
        void update_ShouldExcludeSelfFromConflictCheck() {
            // Given
            Long scheduleId = 1L;
            UpdateScheduleCommand command = new UpdateScheduleCommand(
                    null,
                    LocalTime.of(9, 30), // Slight adjustment
                    LocalTime.of(11, 30),
                    null
            );

            // Return self in potential conflicts - should be excluded
            Page<Schedule> pageWithSelf = new PageImpl<>(List.of(testSchedule));

            when(scheduleRepositoryPort.findById(scheduleId)).thenReturn(Optional.of(testSchedule));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(pageWithSelf);
            when(scheduleRepositoryPort.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Schedule result = scheduleService.update(scheduleId, command);

            // Then
            assertThat(result).isNotNull();
            verify(scheduleRepositoryPort).save(any(Schedule.class));
        }

        @Test
        @DisplayName("Should throw exception when new time range is invalid")
        void update_WhenInvalidTimeRange_ThrowsException() {
            // Given
            Long scheduleId = 1L;
            UpdateScheduleCommand command = new UpdateScheduleCommand(
                    null,
                    LocalTime.of(14, 0),
                    LocalTime.of(12, 0), // End before start
                    null
            );

            when(scheduleRepositoryPort.findById(scheduleId)).thenReturn(Optional.of(testSchedule));

            // When & Then
            assertThatThrownBy(() -> scheduleService.update(scheduleId, command))
                    .isInstanceOf(InvalidScheduleDataException.class)
                    .hasMessageContaining("Start time must be before end time");

            verify(scheduleRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get Schedule Tests")
    class GetScheduleTests {

        @Test
        @DisplayName("Should return schedule when found by ID")
        void getById_WhenScheduleExists_ReturnsSchedule() {
            // Given
            Long scheduleId = 1L;
            when(scheduleRepositoryPort.findById(scheduleId)).thenReturn(Optional.of(testSchedule));

            // When
            Schedule result = scheduleService.getById(scheduleId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testSchedule.getId());
            verify(scheduleRepositoryPort).findById(scheduleId);
        }

        @Test
        @DisplayName("Should throw ScheduleNotFoundException when schedule not found")
        void getById_WhenScheduleNotFound_ThrowsException() {
            // Given
            Long scheduleId = 999L;
            when(scheduleRepositoryPort.findById(scheduleId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> scheduleService.getById(scheduleId))
                    .isInstanceOf(ScheduleNotFoundException.class)
                    .hasMessageContaining("999");

            verify(scheduleRepositoryPort).findById(scheduleId);
        }

        @Test
        @DisplayName("Should return schedules for group")
        void findByGroupId_ReturnsSchedulesForGroup() {
            // Given
            Long groupId = 1L;
            List<Schedule> schedules = List.of(testSchedule);
            when(scheduleRepositoryPort.findByGroupId(groupId)).thenReturn(schedules);

            // When
            List<Schedule> result = scheduleService.findByGroupId(groupId);

            // Then
            assertThat(result).hasSize(1);
            verify(scheduleRepositoryPort).findByGroupId(groupId);
        }

        @Test
        @DisplayName("Should return empty list when no schedules for group")
        void findByGroupId_WhenNoSchedules_ReturnsEmptyList() {
            // Given
            Long groupId = 999L;
            when(scheduleRepositoryPort.findByGroupId(groupId)).thenReturn(Collections.emptyList());

            // When
            List<Schedule> result = scheduleService.findByGroupId(groupId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Delete Schedule Tests")
    class DeleteScheduleTests {

        @Test
        @DisplayName("Should delete schedule successfully")
        void delete_WhenScheduleExists_DeletesSchedule() {
            // Given
            Long scheduleId = 1L;
            when(scheduleRepositoryPort.existsById(scheduleId)).thenReturn(true);

            // When
            scheduleService.delete(scheduleId);

            // Then
            verify(scheduleRepositoryPort).delete(scheduleId);
        }

        @Test
        @DisplayName("Should throw ScheduleNotFoundException when schedule not found")
        void delete_WhenScheduleNotFound_ThrowsException() {
            // Given
            Long scheduleId = 999L;
            when(scheduleRepositoryPort.existsById(scheduleId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> scheduleService.delete(scheduleId))
                    .isInstanceOf(ScheduleNotFoundException.class);

            verify(scheduleRepositoryPort, never()).delete(anyLong());
        }
    }

    @Nested
    @DisplayName("Time Overlap Tests")
    class TimeOverlapTests {

        @Test
        @DisplayName("Should detect overlap when new schedule starts during existing")
        void shouldDetectOverlap_WhenStartsDuringExisting() {
            // Given: Existing 9-11, New 10-12
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0),
                    Classroom.AULA_PORTAL1
            );

            Schedule existing = ScheduleFactory.builder()
                    .id(2L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .buildDomain();

            Page<Schedule> pageWithConflict = new PageImpl<>(List.of(existing));

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(pageWithConflict);

            // When & Then
            assertThatThrownBy(() -> scheduleService.create(command))
                    .isInstanceOf(ScheduleConflictException.class);
        }

        @Test
        @DisplayName("Should detect overlap when new schedule ends during existing")
        void shouldDetectOverlap_WhenEndsDuringExisting() {
            // Given: Existing 10-12, New 9-11
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0),
                    Classroom.AULA_PORTAL1
            );

            Schedule existing = ScheduleFactory.builder()
                    .id(2L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(12, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .buildDomain();

            Page<Schedule> pageWithConflict = new PageImpl<>(List.of(existing));

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(pageWithConflict);

            // When & Then
            assertThatThrownBy(() -> scheduleService.create(command))
                    .isInstanceOf(ScheduleConflictException.class);
        }

        @Test
        @DisplayName("Should detect overlap when new schedule contains existing")
        void shouldDetectOverlap_WhenContainsExisting() {
            // Given: Existing 10-11, New 9-12 (contains existing)
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    LocalTime.of(12, 0),
                    Classroom.AULA_PORTAL1
            );

            Schedule existing = ScheduleFactory.builder()
                    .id(2L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .buildDomain();

            Page<Schedule> pageWithConflict = new PageImpl<>(List.of(existing));

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(pageWithConflict);

            // When & Then
            assertThatThrownBy(() -> scheduleService.create(command))
                    .isInstanceOf(ScheduleConflictException.class);
        }

        @Test
        @DisplayName("Should detect overlap when existing contains new schedule")
        void shouldDetectOverlap_WhenExistingContainsNew() {
            // Given: Existing 9-12, New 10-11 (existing contains new)
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    Classroom.AULA_PORTAL1
            );

            Schedule existing = ScheduleFactory.builder()
                    .id(2L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .buildDomain();

            Page<Schedule> pageWithConflict = new PageImpl<>(List.of(existing));

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(pageWithConflict);

            // When & Then
            assertThatThrownBy(() -> scheduleService.create(command))
                    .isInstanceOf(ScheduleConflictException.class);
        }

        @Test
        @DisplayName("Should not detect overlap when schedules are adjacent")
        void shouldNotDetectOverlap_WhenAdjacentSchedules() {
            // Given: Existing 9-11, New 11-13 (adjacent, no overlap)
            CreateScheduleCommand command = new CreateScheduleCommand(
                    1L,
                    DayOfWeek.MONDAY,
                    LocalTime.of(11, 0),
                    LocalTime.of(13, 0),
                    Classroom.AULA_PORTAL1
            );

            Schedule existing = ScheduleFactory.builder()
                    .id(2L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .classroom(Classroom.AULA_PORTAL1)
                    .buildDomain();

            Page<Schedule> pageWithNoConflict = new PageImpl<>(List.of(existing));

            when(groupRepositoryPort.findById(1L)).thenReturn(Optional.of(testGroup));
            when(scheduleRepositoryPort.findWithFilters(any(ScheduleFilters.class))).thenReturn(pageWithNoConflict);
            when(scheduleRepositoryPort.save(any(Schedule.class))).thenAnswer(invocation -> {
                Schedule s = invocation.getArgument(0);
                return s.toBuilder().id(3L).build();
            });

            // When
            Schedule result = scheduleService.create(command);

            // Then
            assertThat(result).isNotNull();
            verify(scheduleRepositoryPort).save(any(Schedule.class));
        }
    }
}
