package com.acainfo.session.domain.model;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.shared.factory.SessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Session domain entity.
 * Tests query methods and business logic (no Spring context).
 */
@DisplayName("Session Domain Tests")
class SessionTest {

    @Nested
    @DisplayName("Status Query Methods Tests")
    class StatusQueryMethodsTests {

        @Test
        @DisplayName("Should identify scheduled session correctly")
        void isScheduled_WhenStatusIsScheduled_ReturnsTrue() {
            // Given
            Session session = SessionFactory.scheduledRegular();

            // When & Then
            assertThat(session.isScheduled()).isTrue();
            assertThat(session.isInProgress()).isFalse();
            assertThat(session.isCompleted()).isFalse();
            assertThat(session.isCancelled()).isFalse();
            assertThat(session.isPostponed()).isFalse();
        }

        @Test
        @DisplayName("Should identify in-progress session correctly")
        void isInProgress_WhenStatusIsInProgress_ReturnsTrue() {
            // Given
            Session session = SessionFactory.inProgressSession();

            // When & Then
            assertThat(session.isInProgress()).isTrue();
            assertThat(session.isScheduled()).isFalse();
            assertThat(session.isCompleted()).isFalse();
            assertThat(session.isCancelled()).isFalse();
            assertThat(session.isPostponed()).isFalse();
        }

        @Test
        @DisplayName("Should identify completed session correctly")
        void isCompleted_WhenStatusIsCompleted_ReturnsTrue() {
            // Given
            Session session = SessionFactory.completedSession();

            // When & Then
            assertThat(session.isCompleted()).isTrue();
            assertThat(session.isScheduled()).isFalse();
            assertThat(session.isInProgress()).isFalse();
            assertThat(session.isCancelled()).isFalse();
            assertThat(session.isPostponed()).isFalse();
        }

        @Test
        @DisplayName("Should identify cancelled session correctly")
        void isCancelled_WhenStatusIsCancelled_ReturnsTrue() {
            // Given
            Session session = SessionFactory.cancelledSession();

            // When & Then
            assertThat(session.isCancelled()).isTrue();
            assertThat(session.isScheduled()).isFalse();
            assertThat(session.isInProgress()).isFalse();
            assertThat(session.isCompleted()).isFalse();
            assertThat(session.isPostponed()).isFalse();
        }

        @Test
        @DisplayName("Should identify postponed session correctly")
        void isPostponed_WhenStatusIsPostponed_ReturnsTrue() {
            // Given
            Session session = SessionFactory.postponedSession();

            // When & Then
            assertThat(session.isPostponed()).isTrue();
            assertThat(session.isScheduled()).isFalse();
            assertThat(session.isInProgress()).isFalse();
            assertThat(session.isCompleted()).isFalse();
            assertThat(session.isCancelled()).isFalse();
        }
    }

    @Nested
    @DisplayName("Type Query Methods Tests")
    class TypeQueryMethodsTests {

        @Test
        @DisplayName("Should identify regular session correctly")
        void isRegular_WhenTypeIsRegular_ReturnsTrue() {
            // Given
            Session session = SessionFactory.scheduledRegular();

            // When & Then
            assertThat(session.isRegular()).isTrue();
            assertThat(session.isExtra()).isFalse();
            assertThat(session.isSchedulingType()).isFalse();
        }

        @Test
        @DisplayName("Should identify extra session correctly")
        void isExtra_WhenTypeIsExtra_ReturnsTrue() {
            // Given
            Session session = SessionFactory.extraSession();

            // When & Then
            assertThat(session.isExtra()).isTrue();
            assertThat(session.isRegular()).isFalse();
            assertThat(session.isSchedulingType()).isFalse();
        }

        @Test
        @DisplayName("Should identify scheduling session correctly")
        void isSchedulingType_WhenTypeIsScheduling_ReturnsTrue() {
            // Given
            Session session = SessionFactory.schedulingSession();

            // When & Then
            assertThat(session.isSchedulingType()).isTrue();
            assertThat(session.isRegular()).isFalse();
            assertThat(session.isExtra()).isFalse();
        }
    }

    @Nested
    @DisplayName("Mode Query Methods Tests")
    class ModeQueryMethodsTests {

        @Test
        @DisplayName("Should identify in-person session correctly")
        void isInPerson_WhenModeIsInPerson_ReturnsTrue() {
            // Given
            Session session = SessionFactory.builder()
                    .inPerson()
                    .buildDomain();

            // When & Then
            assertThat(session.isInPerson()).isTrue();
            assertThat(session.isOnline()).isFalse();
            assertThat(session.isDual()).isFalse();
        }

        @Test
        @DisplayName("Should identify online session correctly")
        void isOnline_WhenModeIsOnline_ReturnsTrue() {
            // Given
            Session session = SessionFactory.onlineSession();

            // When & Then
            assertThat(session.isOnline()).isTrue();
            assertThat(session.isInPerson()).isFalse();
            assertThat(session.isDual()).isFalse();
        }

        @Test
        @DisplayName("Should identify dual session correctly")
        void isDual_WhenModeIsDual_ReturnsTrue() {
            // Given
            Session session = SessionFactory.dualSession();

            // When & Then
            assertThat(session.isDual()).isTrue();
            assertThat(session.isInPerson()).isFalse();
            assertThat(session.isOnline()).isFalse();
        }
    }

    @Nested
    @DisplayName("Group and Schedule Reference Tests")
    class GroupAndScheduleReferenceTests {

        @Test
        @DisplayName("Should have group for regular session")
        void hasGroup_WhenRegularSession_ReturnsTrue() {
            // Given
            Session session = SessionFactory.scheduledRegular();

            // When & Then
            assertThat(session.hasGroup()).isTrue();
            assertThat(session.getGroupId()).isNotNull();
        }

        @Test
        @DisplayName("Should have group for extra session")
        void hasGroup_WhenExtraSession_ReturnsTrue() {
            // Given
            Session session = SessionFactory.extraSession();

            // When & Then
            assertThat(session.hasGroup()).isTrue();
            assertThat(session.getGroupId()).isNotNull();
        }

        @Test
        @DisplayName("Should not have group for scheduling session")
        void hasGroup_WhenSchedulingSession_ReturnsFalse() {
            // Given
            Session session = SessionFactory.schedulingSession();

            // When & Then
            assertThat(session.hasGroup()).isFalse();
            assertThat(session.getGroupId()).isNull();
        }

        @Test
        @DisplayName("Should have schedule for regular session")
        void hasSchedule_WhenRegularSession_ReturnsTrue() {
            // Given
            Session session = SessionFactory.scheduledRegular();

            // When & Then
            assertThat(session.hasSchedule()).isTrue();
            assertThat(session.getScheduleId()).isNotNull();
        }

        @Test
        @DisplayName("Should not have schedule for extra session")
        void hasSchedule_WhenExtraSession_ReturnsFalse() {
            // Given
            Session session = SessionFactory.extraSession();

            // When & Then
            assertThat(session.hasSchedule()).isFalse();
            assertThat(session.getScheduleId()).isNull();
        }

        @Test
        @DisplayName("Should not have schedule for scheduling session")
        void hasSchedule_WhenSchedulingSession_ReturnsFalse() {
            // Given
            Session session = SessionFactory.schedulingSession();

            // When & Then
            assertThat(session.hasSchedule()).isFalse();
            assertThat(session.getScheduleId()).isNull();
        }
    }

    @Nested
    @DisplayName("Duration Tests")
    class DurationTests {

        @Test
        @DisplayName("Should calculate 2 hour duration correctly")
        void getDurationMinutes_WhenTwoHours_Returns120() {
            // Given
            Session session = SessionFactory.builder()
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .buildDomain();

            // When
            long result = session.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(120);
        }

        @Test
        @DisplayName("Should calculate 1.5 hour duration correctly")
        void getDurationMinutes_WhenOneAndHalfHours_Returns90() {
            // Given
            Session session = SessionFactory.builder()
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(15, 30))
                    .buildDomain();

            // When
            long result = session.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(90);
        }

        @Test
        @DisplayName("Should calculate 30 minute duration correctly")
        void getDurationMinutes_WhenThirtyMinutes_Returns30() {
            // Given
            Session session = SessionFactory.builder()
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 30))
                    .buildDomain();

            // When
            long result = session.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(30);
        }

        @Test
        @DisplayName("Should calculate morning slot duration correctly")
        void getDurationMinutes_MorningSlot_Returns120() {
            // Given
            Session session = SessionFactory.builder()
                    .morningSlot()
                    .buildDomain();

            // When
            long result = session.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(120); // 9:00 - 11:00
        }

        @Test
        @DisplayName("Should calculate afternoon slot duration correctly")
        void getDurationMinutes_AfternoonSlot_Returns120() {
            // Given
            Session session = SessionFactory.builder()
                    .afternoonSlot()
                    .buildDomain();

            // When
            long result = session.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(120); // 16:00 - 18:00
        }

        @Test
        @DisplayName("Should calculate evening slot duration correctly")
        void getDurationMinutes_EveningSlot_Returns120() {
            // Given
            Session session = SessionFactory.builder()
                    .eveningSlot()
                    .buildDomain();

            // When
            long result = session.getDurationMinutes();

            // Then
            assertThat(result).isEqualTo(120); // 18:00 - 20:00
        }
    }

    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should consider sessions equal when ID is the same")
        void equals_WhenSameId_ReturnsTrue() {
            // Given
            Session session1 = SessionFactory.builder()
                    .id(1L)
                    .groupId(1L)
                    .status(SessionStatus.SCHEDULED)
                    .buildDomain();

            Session session2 = SessionFactory.builder()
                    .id(1L) // Same ID
                    .groupId(2L) // Different group
                    .status(SessionStatus.COMPLETED) // Different status
                    .buildDomain();

            // When & Then
            assertThat(session1).isEqualTo(session2);
            assertThat(session1.hashCode()).isEqualTo(session2.hashCode());
        }

        @Test
        @DisplayName("Should consider sessions different when ID differs")
        void equals_WhenDifferentId_ReturnsFalse() {
            // Given
            Session session1 = SessionFactory.builder()
                    .id(1L)
                    .buildDomain();

            Session session2 = SessionFactory.builder()
                    .id(2L)
                    .buildDomain();

            // When & Then
            assertThat(session1).isNotEqualTo(session2);
        }

        @Test
        @DisplayName("Should handle null comparison")
        void equals_WhenComparedToNull_ReturnsFalse() {
            // Given
            Session session = SessionFactory.defaultSession();

            // When & Then
            assertThat(session).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should handle comparison to different class")
        void equals_WhenComparedToDifferentClass_ReturnsFalse() {
            // Given
            Session session = SessionFactory.defaultSession();
            String notASession = "Not a session";

            // When & Then
            assertThat(session).isNotEqualTo(notASession);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build session with all properties")
        void builder_WithAllProperties_CreatesSessionCorrectly() {
            // Given
            LocalDate testDate = LocalDate.of(2025, 1, 15);

            // When
            Session session = Session.builder()
                    .id(1L)
                    .subjectId(5L)
                    .groupId(10L)
                    .scheduleId(15L)
                    .classroom(Classroom.AULA_PORTAL2)
                    .date(testDate)
                    .startTime(LocalTime.of(14, 30))
                    .endTime(LocalTime.of(16, 30))
                    .status(SessionStatus.SCHEDULED)
                    .type(SessionType.REGULAR)
                    .mode(SessionMode.DUAL)
                    .build();

            // Then
            assertThat(session.getId()).isEqualTo(1L);
            assertThat(session.getSubjectId()).isEqualTo(5L);
            assertThat(session.getGroupId()).isEqualTo(10L);
            assertThat(session.getScheduleId()).isEqualTo(15L);
            assertThat(session.getClassroom()).isEqualTo(Classroom.AULA_PORTAL2);
            assertThat(session.getDate()).isEqualTo(testDate);
            assertThat(session.getStartTime()).isEqualTo(LocalTime.of(14, 30));
            assertThat(session.getEndTime()).isEqualTo(LocalTime.of(16, 30));
            assertThat(session.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
            assertThat(session.getType()).isEqualTo(SessionType.REGULAR);
            assertThat(session.getMode()).isEqualTo(SessionMode.DUAL);
        }

        @Test
        @DisplayName("Should support toBuilder for modifications")
        void toBuilder_ShouldAllowModifications() {
            // Given
            Session original = SessionFactory.defaultSession();

            // When
            Session modified = original.toBuilder()
                    .status(SessionStatus.IN_PROGRESS)
                    .classroom(Classroom.AULA_VIRTUAL)
                    .build();

            // Then
            assertThat(modified.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
            assertThat(modified.getClassroom()).isEqualTo(Classroom.AULA_VIRTUAL);
            assertThat(modified.getId()).isEqualTo(original.getId());
            assertThat(modified.getGroupId()).isEqualTo(original.getGroupId());
        }

        @Test
        @DisplayName("Should build session with nullable groupId for scheduling type")
        void builder_WithNullGroupId_CreatesSessionCorrectly() {
            // When
            Session session = Session.builder()
                    .id(1L)
                    .subjectId(5L)
                    .groupId(null) // No group
                    .scheduleId(null) // No schedule
                    .type(SessionType.SCHEDULING)
                    .status(SessionStatus.SCHEDULED)
                    .build();

            // Then
            assertThat(session.getGroupId()).isNull();
            assertThat(session.getScheduleId()).isNull();
            assertThat(session.hasGroup()).isFalse();
            assertThat(session.hasSchedule()).isFalse();
        }
    }

    @Nested
    @DisplayName("Postponed Session Tests")
    class PostponedSessionTests {

        @Test
        @DisplayName("Should have postponedToDate when session is postponed")
        void postponedSession_HasPostponedToDate() {
            // Given
            Session session = SessionFactory.postponedSession();

            // When & Then
            assertThat(session.isPostponed()).isTrue();
            assertThat(session.getPostponedToDate()).isNotNull();
        }

        @Test
        @DisplayName("Should not have postponedToDate when session is not postponed")
        void scheduledSession_HasNoPostponedToDate() {
            // Given
            Session session = SessionFactory.scheduledRegular();

            // When & Then
            assertThat(session.isPostponed()).isFalse();
            assertThat(session.getPostponedToDate()).isNull();
        }
    }

    @Nested
    @DisplayName("Factory Tests")
    class FactoryTests {

        @Test
        @DisplayName("Should create default session with correct properties")
        void defaultSession_HasCorrectDefaults() {
            // When
            Session session = SessionFactory.defaultSession();

            // Then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
            assertThat(session.getType()).isEqualTo(SessionType.REGULAR);
            assertThat(session.getMode()).isEqualTo(SessionMode.IN_PERSON);
            assertThat(session.getClassroom()).isEqualTo(Classroom.AULA_PORTAL1);
            assertThat(session.hasGroup()).isTrue();
            assertThat(session.hasSchedule()).isTrue();
            assertThat(session.getDurationMinutes()).isEqualTo(120);
        }

        @Test
        @DisplayName("Should create extra session correctly")
        void extraSession_HasCorrectProperties() {
            // When
            Session session = SessionFactory.extraSession();

            // Then
            assertThat(session.isExtra()).isTrue();
            assertThat(session.hasGroup()).isTrue();
            assertThat(session.hasSchedule()).isFalse();
        }

        @Test
        @DisplayName("Should create scheduling session correctly")
        void schedulingSession_HasCorrectProperties() {
            // When
            Session session = SessionFactory.schedulingSession();

            // Then
            assertThat(session.isSchedulingType()).isTrue();
            assertThat(session.hasGroup()).isFalse();
            assertThat(session.hasSchedule()).isFalse();
            assertThat(session.getSubjectId()).isNotNull();
        }

        @Test
        @DisplayName("Should create online session correctly")
        void onlineSession_HasCorrectProperties() {
            // When
            Session session = SessionFactory.onlineSession();

            // Then
            assertThat(session.isOnline()).isTrue();
            assertThat(session.getClassroom()).isEqualTo(Classroom.AULA_VIRTUAL);
        }

        @Test
        @DisplayName("Should create session for specific group")
        void forGroup_CreatesSessionWithSpecifiedGroup() {
            // Given
            Long groupId = 42L;

            // When
            Session session = SessionFactory.forGroup(groupId);

            // Then
            assertThat(session.getGroupId()).isEqualTo(groupId);
        }

        @Test
        @DisplayName("Should create session for specific subject")
        void forSubject_CreatesSessionWithSpecifiedSubject() {
            // Given
            Long subjectId = 99L;

            // When
            Session session = SessionFactory.forSubject(subjectId);

            // Then
            assertThat(session.getSubjectId()).isEqualTo(subjectId);
        }

        @Test
        @DisplayName("Should create session for specific schedule")
        void forSchedule_CreatesSessionWithSpecifiedSchedule() {
            // Given
            Long scheduleId = 77L;

            // When
            Session session = SessionFactory.forSchedule(scheduleId);

            // Then
            assertThat(session.getScheduleId()).isEqualTo(scheduleId);
        }

        @Test
        @DisplayName("Should create session on specific date")
        void onDate_CreatesSessionWithSpecifiedDate() {
            // Given
            LocalDate date = LocalDate.of(2025, 6, 15);

            // When
            Session session = SessionFactory.onDate(date);

            // Then
            assertThat(session.getDate()).isEqualTo(date);
        }

        @Test
        @DisplayName("Should create morning session tomorrow")
        void morningSessionTomorrow_HasCorrectProperties() {
            // When
            Session session = SessionFactory.morningSessionTomorrow();

            // Then
            assertThat(session.getDate()).isEqualTo(LocalDate.now().plusDays(1));
            assertThat(session.getStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(session.getEndTime()).isEqualTo(LocalTime.of(11, 0));
            assertThat(session.getClassroom()).isEqualTo(Classroom.AULA_PORTAL1);
        }

        @Test
        @DisplayName("Should create afternoon session tomorrow")
        void afternoonSessionTomorrow_HasCorrectProperties() {
            // When
            Session session = SessionFactory.afternoonSessionTomorrow();

            // Then
            assertThat(session.getDate()).isEqualTo(LocalDate.now().plusDays(1));
            assertThat(session.getStartTime()).isEqualTo(LocalTime.of(16, 0));
            assertThat(session.getEndTime()).isEqualTo(LocalTime.of(18, 0));
            assertThat(session.getClassroom()).isEqualTo(Classroom.AULA_PORTAL2);
        }
    }

    @Nested
    @DisplayName("SessionStatus Enum Tests")
    class SessionStatusEnumTests {

        @Test
        @DisplayName("Should have all expected status values")
        void sessionStatus_HasAllExpectedValues() {
            assertThat(SessionStatus.values()).containsExactlyInAnyOrder(
                    SessionStatus.SCHEDULED,
                    SessionStatus.IN_PROGRESS,
                    SessionStatus.COMPLETED,
                    SessionStatus.CANCELLED,
                    SessionStatus.POSTPONED
            );
        }
    }

    @Nested
    @DisplayName("SessionType Enum Tests")
    class SessionTypeEnumTests {

        @Test
        @DisplayName("Should have all expected type values")
        void sessionType_HasAllExpectedValues() {
            assertThat(SessionType.values()).containsExactlyInAnyOrder(
                    SessionType.REGULAR,
                    SessionType.EXTRA,
                    SessionType.SCHEDULING
            );
        }
    }

    @Nested
    @DisplayName("SessionMode Enum Tests")
    class SessionModeEnumTests {

        @Test
        @DisplayName("Should have all expected mode values")
        void sessionMode_HasAllExpectedValues() {
            assertThat(SessionMode.values()).containsExactlyInAnyOrder(
                    SessionMode.IN_PERSON,
                    SessionMode.ONLINE,
                    SessionMode.DUAL
            );
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include status in toString")
        void toString_IncludesStatus() {
            // Given
            Session session = SessionFactory.builder()
                    .status(SessionStatus.IN_PROGRESS)
                    .buildDomain();

            // When
            String sessionString = session.toString();

            // Then
            assertThat(sessionString).contains("IN_PROGRESS");
        }

        @Test
        @DisplayName("Should include type in toString")
        void toString_IncludesType() {
            // Given
            Session session = SessionFactory.builder()
                    .type(SessionType.EXTRA)
                    .buildDomain();

            // When
            String sessionString = session.toString();

            // Then
            assertThat(sessionString).contains("EXTRA");
        }

        @Test
        @DisplayName("Should include classroom in toString")
        void toString_IncludesClassroom() {
            // Given
            Session session = SessionFactory.builder()
                    .classroom(Classroom.AULA_PORTAL2)
                    .buildDomain();

            // When
            String sessionString = session.toString();

            // Then
            assertThat(sessionString).contains("AULA_PORTAL2");
        }
    }
}
