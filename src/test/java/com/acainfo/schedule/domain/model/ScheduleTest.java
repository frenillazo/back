package com.acainfo.schedule.domain.model;

import com.acainfo.shared.factory.ScheduleFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Schedule domain entity.
 * Tests query methods and business logic (no Spring context).
 */
@DisplayName("Schedule Domain Tests")
class ScheduleTest {

    @Nested
    @DisplayName("Classroom Type Tests")
    class ClassroomTypeTests {

        @Test
        @DisplayName("Should identify physical classroom Portal 1 correctly")
        void isPhysical_WhenAulaPortal1_ReturnsTrue() {
            // Given
            Schedule schedule = ScheduleFactory.builder()
                    .classroom(Classroom.AULA_PORTAL1)
                    .buildDomain();

            // When
            boolean result = schedule.isPhysical();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should identify physical classroom Portal 2 correctly")
        void isPhysical_WhenAulaPortal2_ReturnsTrue() {
            // Given
            Schedule schedule = ScheduleFactory.builder()
                    .classroom(Classroom.AULA_PORTAL2)
                    .buildDomain();

            // When
            boolean result = schedule.isPhysical();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify virtual classroom as physical")
        void isPhysical_WhenAulaVirtual_ReturnsFalse() {
            // Given
            Schedule schedule = ScheduleFactory.virtual();

            // When
            boolean result = schedule.isPhysical();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should identify online classroom correctly")
        void isOnline_WhenAulaVirtual_ReturnsTrue() {
            // Given
            Schedule schedule = ScheduleFactory.virtual();

            // When
            boolean result = schedule.isOnline();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify physical classroom as online")
        void isOnline_WhenAulaPortal1_ReturnsFalse() {
            // Given
            Schedule schedule = ScheduleFactory.inPortal1();

            // When
            boolean result = schedule.isOnline();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should not identify Portal 2 as online")
        void isOnline_WhenAulaPortal2_ReturnsFalse() {
            // Given
            Schedule schedule = ScheduleFactory.inPortal2();

            // When
            boolean result = schedule.isOnline();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Duration Tests")
    class DurationTests {

        @Test
        @DisplayName("Should calculate 2 hour duration correctly")
        void getDurationMinutes_WhenTwoHours_Returns120() {
            // Given
            Schedule schedule = ScheduleFactory.builder()
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .buildDomain();

            // When
            long result = schedule.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(120);
        }

        @Test
        @DisplayName("Should calculate 1.5 hour duration correctly")
        void getDurationMinutes_WhenOneAndHalfHours_Returns90() {
            // Given
            Schedule schedule = ScheduleFactory.builder()
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 30))
                    .buildDomain();

            // When
            long result = schedule.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(90);
        }

        @Test
        @DisplayName("Should calculate 30 minute duration correctly")
        void getDurationMinutes_WhenThirtyMinutes_Returns30() {
            // Given
            Schedule schedule = ScheduleFactory.builder()
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 30))
                    .buildDomain();

            // When
            long result = schedule.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(30);
        }

        @Test
        @DisplayName("Should calculate 3 hour duration correctly")
        void getDurationMinutes_WhenThreeHours_Returns180() {
            // Given
            Schedule schedule = ScheduleFactory.builder()
                    .startTime(LocalTime.of(16, 0))
                    .endTime(LocalTime.of(19, 0))
                    .buildDomain();

            // When
            long result = schedule.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(180);
        }

        @Test
        @DisplayName("Should calculate morning slot duration correctly")
        void getDurationMinutes_MorningSlot_Returns120() {
            // Given
            Schedule schedule = ScheduleFactory.morningSchedule();

            // When
            long result = schedule.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(120); // 9:00 - 11:00
        }

        @Test
        @DisplayName("Should calculate afternoon slot duration correctly")
        void getDurationMinutes_AfternoonSlot_Returns120() {
            // Given
            Schedule schedule = ScheduleFactory.afternoonSchedule();

            // When
            long result = schedule.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(120); // 16:00 - 18:00
        }

        @Test
        @DisplayName("Should calculate evening slot duration correctly")
        void getDurationMinutes_EveningSlot_Returns120() {
            // Given
            Schedule schedule = ScheduleFactory.eveningSchedule();

            // When
            long result = schedule.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(120); // 18:00 - 20:00
        }
    }

    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should consider schedules equal when ID is the same")
        void equals_WhenSameId_ReturnsTrue() {
            // Given
            Schedule schedule1 = ScheduleFactory.builder()
                    .id(1L)
                    .groupId(1L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .buildDomain();

            Schedule schedule2 = ScheduleFactory.builder()
                    .id(1L) // Same ID
                    .groupId(2L) // Different group
                    .dayOfWeek(DayOfWeek.TUESDAY) // Different day
                    .buildDomain();

            // When & Then
            assertThat(schedule1).isEqualTo(schedule2);
            assertThat(schedule1.hashCode()).isEqualTo(schedule2.hashCode());
        }

        @Test
        @DisplayName("Should consider schedules different when ID differs")
        void equals_WhenDifferentId_ReturnsFalse() {
            // Given
            Schedule schedule1 = ScheduleFactory.builder()
                    .id(1L)
                    .buildDomain();

            Schedule schedule2 = ScheduleFactory.builder()
                    .id(2L)
                    .buildDomain();

            // When & Then
            assertThat(schedule1).isNotEqualTo(schedule2);
        }

        @Test
        @DisplayName("Should handle null comparison")
        void equals_WhenComparedToNull_ReturnsFalse() {
            // Given
            Schedule schedule = ScheduleFactory.defaultSchedule();

            // When & Then
            assertThat(schedule).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should handle comparison to different class")
        void equals_WhenComparedToDifferentClass_ReturnsFalse() {
            // Given
            Schedule schedule = ScheduleFactory.defaultSchedule();
            String notASchedule = "Not a schedule";

            // When & Then
            assertThat(schedule).isNotEqualTo(notASchedule);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build schedule with all properties")
        void builder_WithAllProperties_CreatesScheduleCorrectly() {
            // When
            Schedule schedule = Schedule.builder()
                    .id(1L)
                    .groupId(5L)
                    .dayOfWeek(DayOfWeek.WEDNESDAY)
                    .startTime(LocalTime.of(14, 30))
                    .endTime(LocalTime.of(16, 30))
                    .classroom(Classroom.AULA_PORTAL2)
                    .build();

            // Then
            assertThat(schedule.getId()).isEqualTo(1L);
            assertThat(schedule.getGroupId()).isEqualTo(5L);
            assertThat(schedule.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
            assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(14, 30));
            assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(16, 30));
            assertThat(schedule.getClassroom()).isEqualTo(Classroom.AULA_PORTAL2);
        }

        @Test
        @DisplayName("Should support toBuilder for modifications")
        void toBuilder_ShouldAllowModifications() {
            // Given
            Schedule original = ScheduleFactory.defaultSchedule();

            // When
            Schedule modified = original.toBuilder()
                    .dayOfWeek(DayOfWeek.FRIDAY)
                    .classroom(Classroom.AULA_VIRTUAL)
                    .build();

            // Then
            assertThat(modified.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
            assertThat(modified.getClassroom()).isEqualTo(Classroom.AULA_VIRTUAL);
            assertThat(modified.getId()).isEqualTo(original.getId());
            assertThat(modified.getGroupId()).isEqualTo(original.getGroupId());
        }
    }

    @Nested
    @DisplayName("Factory Tests")
    class FactoryTests {

        @Test
        @DisplayName("Should create default schedule with correct properties")
        void defaultSchedule_HasCorrectDefaults() {
            // When
            Schedule schedule = ScheduleFactory.defaultSchedule();

            // Then
            assertThat(schedule.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(11, 0));
            assertThat(schedule.getClassroom()).isEqualTo(Classroom.AULA_PORTAL1);
            assertThat(schedule.isPhysical()).isTrue();
            assertThat(schedule.getDurationMinutes()).isEqualTo(120);
        }

        @Test
        @DisplayName("Should create Portal 1 schedule correctly")
        void inPortal1_HasCorrectClassroom() {
            // When
            Schedule schedule = ScheduleFactory.inPortal1();

            // Then
            assertThat(schedule.getClassroom()).isEqualTo(Classroom.AULA_PORTAL1);
            assertThat(schedule.isPhysical()).isTrue();
        }

        @Test
        @DisplayName("Should create Portal 2 schedule correctly")
        void inPortal2_HasCorrectClassroom() {
            // When
            Schedule schedule = ScheduleFactory.inPortal2();

            // Then
            assertThat(schedule.getClassroom()).isEqualTo(Classroom.AULA_PORTAL2);
            assertThat(schedule.isPhysical()).isTrue();
        }

        @Test
        @DisplayName("Should create virtual schedule correctly")
        void virtual_HasCorrectClassroom() {
            // When
            Schedule schedule = ScheduleFactory.virtual();

            // Then
            assertThat(schedule.getClassroom()).isEqualTo(Classroom.AULA_VIRTUAL);
            assertThat(schedule.isOnline()).isTrue();
            assertThat(schedule.isPhysical()).isFalse();
        }

        @Test
        @DisplayName("Should create morning schedule correctly")
        void morningSchedule_HasCorrectTime() {
            // When
            Schedule schedule = ScheduleFactory.morningSchedule();

            // Then
            assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(11, 0));
        }

        @Test
        @DisplayName("Should create afternoon schedule correctly")
        void afternoonSchedule_HasCorrectTime() {
            // When
            Schedule schedule = ScheduleFactory.afternoonSchedule();

            // Then
            assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(16, 0));
            assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(18, 0));
        }

        @Test
        @DisplayName("Should create evening schedule correctly")
        void eveningSchedule_HasCorrectTime() {
            // When
            Schedule schedule = ScheduleFactory.eveningSchedule();

            // Then
            assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(18, 0));
            assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(20, 0));
        }

        @Test
        @DisplayName("Should create schedule for specific group")
        void forGroup_CreatesScheduleWithSpecifiedGroup() {
            // Given
            Long groupId = 42L;

            // When
            Schedule schedule = ScheduleFactory.forGroup(groupId);

            // Then
            assertThat(schedule.getGroupId()).isEqualTo(groupId);
        }

        @Test
        @DisplayName("Should create schedule on specific day")
        void onDay_CreatesScheduleWithSpecifiedDay() {
            // When
            Schedule schedule = ScheduleFactory.onDay(DayOfWeek.FRIDAY);

            // Then
            assertThat(schedule.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        }

        @Test
        @DisplayName("Should create schedule with specific time")
        void withTime_CreatesScheduleWithSpecifiedTime() {
            // Given
            LocalTime start = LocalTime.of(10, 30);
            LocalTime end = LocalTime.of(12, 30);

            // When
            Schedule schedule = ScheduleFactory.withTime(start, end);

            // Then
            assertThat(schedule.getStartTime()).isEqualTo(start);
            assertThat(schedule.getEndTime()).isEqualTo(end);
        }

        @Test
        @DisplayName("Should create Monday morning Portal 1 schedule")
        void mondayMorningPortal1_HasCorrectProperties() {
            // When
            Schedule schedule = ScheduleFactory.mondayMorningPortal1();

            // Then
            assertThat(schedule.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(11, 0));
            assertThat(schedule.getClassroom()).isEqualTo(Classroom.AULA_PORTAL1);
        }

        @Test
        @DisplayName("Should create Tuesday afternoon Portal 2 schedule")
        void tuesdayAfternoonPortal2_HasCorrectProperties() {
            // When
            Schedule schedule = ScheduleFactory.tuesdayAfternoonPortal2();

            // Then
            assertThat(schedule.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
            assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(16, 0));
            assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(18, 0));
            assertThat(schedule.getClassroom()).isEqualTo(Classroom.AULA_PORTAL2);
        }

        @Test
        @DisplayName("Should create conflicting schedule correctly")
        void conflictingWithDefault_HasSameTimeAndClassroom() {
            // Given
            Schedule defaultSchedule = ScheduleFactory.defaultSchedule();

            // When
            Schedule conflicting = ScheduleFactory.conflictingWithDefault();

            // Then
            assertThat(conflicting.getId()).isNotEqualTo(defaultSchedule.getId());
            assertThat(conflicting.getGroupId()).isNotEqualTo(defaultSchedule.getGroupId());
            assertThat(conflicting.getDayOfWeek()).isEqualTo(defaultSchedule.getDayOfWeek());
            assertThat(conflicting.getStartTime()).isEqualTo(defaultSchedule.getStartTime());
            assertThat(conflicting.getEndTime()).isEqualTo(defaultSchedule.getEndTime());
            assertThat(conflicting.getClassroom()).isEqualTo(defaultSchedule.getClassroom());
        }
    }

    @Nested
    @DisplayName("Classroom Enum Tests")
    class ClassroomEnumTests {

        @Test
        @DisplayName("Should have correct code for Portal 1")
        void aulaPortal1_HasCorrectCode() {
            assertThat(Classroom.AULA_PORTAL1.getCode()).isEqualTo("aula_portal1");
        }

        @Test
        @DisplayName("Should have correct code for Portal 2")
        void aulaPortal2_HasCorrectCode() {
            assertThat(Classroom.AULA_PORTAL2.getCode()).isEqualTo("aula_portal2");
        }

        @Test
        @DisplayName("Should have correct code for Virtual")
        void aulaVirtual_HasCorrectCode() {
            assertThat(Classroom.AULA_VIRTUAL.getCode()).isEqualTo("aulavirtual");
        }

        @Test
        @DisplayName("Should have correct display name for Portal 1")
        void aulaPortal1_HasCorrectDisplayName() {
            assertThat(Classroom.AULA_PORTAL1.getDisplayName()).isEqualTo("Aula Portal 1");
        }

        @Test
        @DisplayName("Should have correct display name for Portal 2")
        void aulaPortal2_HasCorrectDisplayName() {
            assertThat(Classroom.AULA_PORTAL2.getDisplayName()).isEqualTo("Aula Portal 2");
        }

        @Test
        @DisplayName("Should have correct display name for Virtual")
        void aulaVirtual_HasCorrectDisplayName() {
            assertThat(Classroom.AULA_VIRTUAL.getDisplayName()).isEqualTo("Aula Virtual");
        }

        @Test
        @DisplayName("Should have correct capacity for Portal 1")
        void aulaPortal1_HasCorrectCapacity() {
            assertThat(Classroom.AULA_PORTAL1.getCapacity()).isEqualTo(24);
        }

        @Test
        @DisplayName("Should have correct capacity for Portal 2")
        void aulaPortal2_HasCorrectCapacity() {
            assertThat(Classroom.AULA_PORTAL2.getCapacity()).isEqualTo(24);
        }

        @Test
        @DisplayName("Should have null capacity for Virtual (unlimited)")
        void aulaVirtual_HasNullCapacity() {
            assertThat(Classroom.AULA_VIRTUAL.getCapacity()).isNull();
        }

        @Test
        @DisplayName("Should correctly identify physical classrooms")
        void physicalClassrooms_AreIdentifiedCorrectly() {
            assertThat(Classroom.AULA_PORTAL1.isPhysical()).isTrue();
            assertThat(Classroom.AULA_PORTAL2.isPhysical()).isTrue();
            assertThat(Classroom.AULA_VIRTUAL.isPhysical()).isFalse();
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include day of week in toString")
        void toString_IncludesDayOfWeek() {
            // Given
            Schedule schedule = ScheduleFactory.builder()
                    .dayOfWeek(DayOfWeek.WEDNESDAY)
                    .buildDomain();

            // When
            String scheduleString = schedule.toString();

            // Then
            assertThat(scheduleString).contains("WEDNESDAY");
        }

        @Test
        @DisplayName("Should include classroom in toString")
        void toString_IncludesClassroom() {
            // Given
            Schedule schedule = ScheduleFactory.builder()
                    .classroom(Classroom.AULA_PORTAL2)
                    .buildDomain();

            // When
            String scheduleString = schedule.toString();

            // Then
            assertThat(scheduleString).contains("AULA_PORTAL2");
        }
    }
}
