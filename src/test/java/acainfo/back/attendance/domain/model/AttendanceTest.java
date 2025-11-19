package acainfo.back.attendance.domain.model;

import acainfo.back.session.domain.model.Session;
import acainfo.back.session.domain.model.SessionStatus;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Attendance entity
 */
@DisplayName("Attendance Entity Tests")
class AttendanceTest {

    private Session session;
    private Long studentId;
    private Long teacherId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Create a completed session
        session = Session.builder()
            .id(1L)
            .subjectGroup(SubjectGroup.builder().id(1L).build())
            .scheduledStart(now.minusHours(2))
            .scheduledEnd(now.minusHours(1))
            .status(SessionStatus.COMPLETADA)
            .build();

        studentId = 100L;
        teacherId = 200L;
    }

    @Nested
    @DisplayName("Constructor and Builder Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create attendance with builder")
        void shouldCreateAttendanceWithBuilder() {
            // When
            Attendance attendance = Attendance.builder()
                .session(session)
                .studentId(studentId)
                .status(AttendanceStatus.PRESENTE)
                .recordedAt(now)
                .recordedById(teacherId)
                .build();

            // Then
            assertThat(attendance).isNotNull();
            assertThat(attendance.getSession()).isEqualTo(session);
            assertThat(attendance.getStudentId()).isEqualTo(studentId);
            assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENTE);
            assertThat(attendance.getRecordedById()).isEqualTo(teacherId);
        }

        @Test
        @DisplayName("Should use default status PRESENTE")
        void shouldUseDefaultStatusPresente() {
            // When
            Attendance attendance = Attendance.builder()
                .session(session)
                .studentId(studentId)
                .recordedById(teacherId)
                .build();

            // Then
            assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENTE);
        }

        @Test
        @DisplayName("Should use default recordedAt to now")
        void shouldUseDefaultRecordedAt() {
            // When
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            Attendance attendance = Attendance.builder()
                .session(session)
                .studentId(studentId)
                .recordedById(teacherId)
                .build();
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            // Then
            assertThat(attendance.getRecordedAt()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("Status Check Methods Tests")
    class StatusCheckTests {

        @Test
        @DisplayName("isPresent should return true when status is PRESENTE")
        void isPresent_shouldReturnTrueWhenStatusIsPresente() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);

            // Then
            assertThat(attendance.isPresent()).isTrue();
            assertThat(attendance.isAbsent()).isFalse();
            assertThat(attendance.wasLate()).isFalse();
            assertThat(attendance.isJustified()).isFalse();
        }

        @Test
        @DisplayName("isAbsent should return true when status is AUSENTE")
        void isAbsent_shouldReturnTrueWhenStatusIsAusente() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.AUSENTE);

            // Then
            assertThat(attendance.isAbsent()).isTrue();
            assertThat(attendance.isPresent()).isFalse();
            assertThat(attendance.wasLate()).isFalse();
            assertThat(attendance.isJustified()).isFalse();
        }

        @Test
        @DisplayName("wasLate should return true when status is TARDANZA")
        void wasLate_shouldReturnTrueWhenStatusIsTardanza() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.TARDANZA);

            // Then
            assertThat(attendance.wasLate()).isTrue();
            assertThat(attendance.isPresent()).isFalse();
            assertThat(attendance.isAbsent()).isFalse();
            assertThat(attendance.isJustified()).isFalse();
        }

        @Test
        @DisplayName("isJustified should return true when status is JUSTIFICADO")
        void isJustified_shouldReturnTrueWhenStatusIsJustificado() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.JUSTIFICADO);

            // Then
            assertThat(attendance.isJustified()).isTrue();
            assertThat(attendance.isPresent()).isFalse();
            assertThat(attendance.isAbsent()).isFalse();
            assertThat(attendance.wasLate()).isFalse();
        }

        @Test
        @DisplayName("countsAsEffectiveAttendance should return true for PRESENTE and TARDANZA")
        void countsAsEffectiveAttendance_shouldReturnTrueForPresenteAndTardanza() {
            // Given
            Attendance presente = createAttendance(AttendanceStatus.PRESENTE);
            Attendance tardanza = createAttendance(AttendanceStatus.TARDANZA);
            Attendance ausente = createAttendance(AttendanceStatus.AUSENTE);
            Attendance justificado = createAttendance(AttendanceStatus.JUSTIFICADO);

            // Then
            assertThat(presente.countsAsEffectiveAttendance()).isTrue();
            assertThat(tardanza.countsAsEffectiveAttendance()).isTrue();
            assertThat(ausente.countsAsEffectiveAttendance()).isFalse();
            assertThat(justificado.countsAsEffectiveAttendance()).isFalse();
        }

        @Test
        @DisplayName("isAnyAbsence should return true for AUSENTE and JUSTIFICADO")
        void isAnyAbsence_shouldReturnTrueForAusenteAndJustificado() {
            // Given
            Attendance ausente = createAttendance(AttendanceStatus.AUSENTE);
            Attendance justificado = createAttendance(AttendanceStatus.JUSTIFICADO);
            Attendance presente = createAttendance(AttendanceStatus.PRESENTE);
            Attendance tardanza = createAttendance(AttendanceStatus.TARDANZA);

            // Then
            assertThat(ausente.isAnyAbsence()).isTrue();
            assertThat(justificado.isAnyAbsence()).isTrue();
            assertThat(presente.isAnyAbsence()).isFalse();
            assertThat(tardanza.isAnyAbsence()).isFalse();
        }
    }

    @Nested
    @DisplayName("State Transition Tests")
    class StateTransitionTests {

        @Test
        @DisplayName("markAsPresent should change status to PRESENTE")
        void markAsPresent_shouldChangeStatusToPresente() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.AUSENTE);
            attendance.setMinutesLate(15);
            attendance.setJustifiedAt(now);

            // When
            attendance.markAsPresent();

            // Then
            assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENTE);
            assertThat(attendance.getMinutesLate()).isNull();
            assertThat(attendance.getJustifiedAt()).isNull();
            assertThat(attendance.getJustifiedById()).isNull();
        }

        @Test
        @DisplayName("markAsAbsent should change status to AUSENTE with reason")
        void markAsAbsent_shouldChangeStatusToAusente() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);
            String reason = "Student did not attend";

            // When
            attendance.markAsAbsent(reason);

            // Then
            assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.AUSENTE);
            assertThat(attendance.getNotes()).isEqualTo(reason);
            assertThat(attendance.getMinutesLate()).isNull();
            assertThat(attendance.getJustifiedAt()).isNull();
        }

        @Test
        @DisplayName("markAsLate should change status to TARDANZA with minutes")
        void markAsLate_shouldChangeStatusToTardanza() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);
            int minutesLate = 20;
            String notes = "Traffic delay";

            // When
            attendance.markAsLate(minutesLate, notes);

            // Then
            assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.TARDANZA);
            assertThat(attendance.getMinutesLate()).isEqualTo(minutesLate);
            assertThat(attendance.getNotes()).isEqualTo(notes);
        }

        @Test
        @DisplayName("markAsLate should throw exception if minutes <= 0")
        void markAsLate_shouldThrowExceptionIfMinutesInvalid() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);

            // When/Then
            assertThatThrownBy(() -> attendance.markAsLate(0, "notes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Minutes late must be greater than 0");

            assertThatThrownBy(() -> attendance.markAsLate(-5, "notes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Minutes late must be greater than 0");
        }

        @Test
        @DisplayName("justify should change AUSENTE to JUSTIFICADO")
        void justify_shouldChangeAusenteToJustificado() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.AUSENTE);
            Long justifierUserId = 999L;
            String reason = "Medical certificate provided";

            // When
            LocalDateTime before = LocalDateTime.now();
            attendance.justify(justifierUserId, reason);
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            // Then
            assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.JUSTIFICADO);
            assertThat(attendance.getJustifiedById()).isEqualTo(justifierUserId);
            assertThat(attendance.getNotes()).isEqualTo(reason);
            assertThat(attendance.getJustifiedAt()).isBetween(before, after);
            assertThat(attendance.getMinutesLate()).isNull();
        }

        @Test
        @DisplayName("justify should throw exception if status is not AUSENTE")
        void justify_shouldThrowExceptionIfNotAusente() {
            // Given
            Attendance presente = createAttendance(AttendanceStatus.PRESENTE);
            Attendance tardanza = createAttendance(AttendanceStatus.TARDANZA);
            Attendance justificado = createAttendance(AttendanceStatus.JUSTIFICADO);

            // When/Then
            assertThatThrownBy(() -> presente.justify(999L, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot justify")
                .hasMessageContaining("PRESENTE");

            assertThatThrownBy(() -> tardanza.justify(999L, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot justify")
                .hasMessageContaining("TARDANZA");

            assertThatThrownBy(() -> justificado.justify(999L, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot justify")
                .hasMessageContaining("JUSTIFICADO");
        }

        @Test
        @DisplayName("updateStatus should change status and clear irrelevant fields")
        void updateStatus_shouldChangeStatusAndClearFields() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.TARDANZA);
            attendance.setMinutesLate(20);
            attendance.setNotes("Old notes");

            // When
            attendance.updateStatus(AttendanceStatus.PRESENTE, "New notes");

            // Then
            assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENTE);
            assertThat(attendance.getNotes()).isEqualTo("New notes");
            assertThat(attendance.getMinutesLate()).isNull();
        }

        @Test
        @DisplayName("updateStatus should throw exception if status is null")
        void updateStatus_shouldThrowExceptionIfStatusIsNull() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);

            // When/Then
            assertThatThrownBy(() -> attendance.updateStatus(null, "notes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Status cannot be null");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("isMinutesLateValid should return true when minutesLate is set with TARDANZA")
        void isMinutesLateValid_shouldReturnTrueWhenValidWithTardanza() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.TARDANZA);
            attendance.setMinutesLate(15);

            // Then
            assertThat(attendance.isMinutesLateValid()).isTrue();
        }

        @Test
        @DisplayName("isMinutesLateValid should return false when minutesLate is set without TARDANZA")
        void isMinutesLateValid_shouldReturnFalseWhenSetWithoutTardanza() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);
            attendance.setMinutesLate(15);

            // Then
            assertThat(attendance.isMinutesLateValid()).isFalse();
        }

        @Test
        @DisplayName("isMinutesLateValid should return true when minutesLate is null")
        void isMinutesLateValid_shouldReturnTrueWhenNull() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);
            attendance.setMinutesLate(null);

            // Then
            assertThat(attendance.isMinutesLateValid()).isTrue();
        }

        @Test
        @DisplayName("isJustificationValid should return true when justification fields set with JUSTIFICADO")
        void isJustificationValid_shouldReturnTrueWhenValidWithJustificado() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.JUSTIFICADO);
            attendance.setJustifiedAt(now);
            attendance.setJustifiedById(999L);

            // Then
            assertThat(attendance.isJustificationValid()).isTrue();
        }

        @Test
        @DisplayName("isJustificationValid should return false when justification fields set without JUSTIFICADO")
        void isJustificationValid_shouldReturnFalseWhenSetWithoutJustificado() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);
            attendance.setJustifiedAt(now);
            attendance.setJustifiedById(999L);

            // Then
            assertThat(attendance.isJustificationValid()).isFalse();
        }

        @Test
        @DisplayName("isJustificationValid should return true when justification fields are null")
        void isJustificationValid_shouldReturnTrueWhenNull() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);
            attendance.setJustifiedAt(null);
            attendance.setJustifiedById(null);

            // Then
            assertThat(attendance.isJustificationValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("getDescription should return human-readable description")
        void getDescription_shouldReturnReadableDescription() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);

            // When
            String description = attendance.getDescription();

            // Then
            assertThat(description)
                .contains("Student " + studentId)
                .contains("Session " + session.getId())
                .contains("Status: PRESENTE");
        }

        @Test
        @DisplayName("getDescription should include minutes late for TARDANZA")
        void getDescription_shouldIncludeMinutesLateForTardanza() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.TARDANZA);
            attendance.setMinutesLate(20);

            // When
            String description = attendance.getDescription();

            // Then
            assertThat(description)
                .contains("20 min late");
        }

        @Test
        @DisplayName("canBeModified should return true if recorded within 7 days")
        void canBeModified_shouldReturnTrueIfWithin7Days() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);
            attendance.setRecordedAt(LocalDateTime.now().minusDays(6));

            // Then
            assertThat(attendance.canBeModified()).isTrue();
        }

        @Test
        @DisplayName("canBeModified should return false if recorded more than 7 days ago")
        void canBeModified_shouldReturnFalseIfOver7Days() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);
            attendance.setRecordedAt(LocalDateTime.now().minusDays(8));

            // Then
            assertThat(attendance.canBeModified()).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityTests {

        @Test
        @DisplayName("Two attendance records with same ID should be equal")
        void twoAttendanceWithSameId_shouldBeEqual() {
            // Given
            Attendance attendance1 = createAttendance(AttendanceStatus.PRESENTE);
            attendance1.setId(1L);

            Attendance attendance2 = createAttendance(AttendanceStatus.AUSENTE);
            attendance2.setId(1L);

            // Then
            assertThat(attendance1).isEqualTo(attendance2);
            assertThat(attendance1.hashCode()).isEqualTo(attendance2.hashCode());
        }

        @Test
        @DisplayName("Two attendance records with different IDs should not be equal")
        void twoAttendanceWithDifferentIds_shouldNotBeEqual() {
            // Given
            Attendance attendance1 = createAttendance(AttendanceStatus.PRESENTE);
            attendance1.setId(1L);

            Attendance attendance2 = createAttendance(AttendanceStatus.PRESENTE);
            attendance2.setId(2L);

            // Then
            assertThat(attendance1).isNotEqualTo(attendance2);
        }

        @Test
        @DisplayName("Attendance should equal itself")
        void attendance_shouldEqualItself() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);
            attendance.setId(1L);

            // Then
            assertThat(attendance).isEqualTo(attendance);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain relevant information")
        void toString_shouldContainRelevantInfo() {
            // Given
            Attendance attendance = createAttendance(AttendanceStatus.PRESENTE);
            attendance.setId(123L);

            // When
            String result = attendance.toString();

            // Then
            assertThat(result)
                .contains("Attendance{")
                .contains("id=123")
                .contains("sessionId=" + session.getId())
                .contains("studentId=" + studentId)
                .contains("status=PRESENTE");
        }
    }

    // ==================== HELPER METHODS ====================

    private Attendance createAttendance(AttendanceStatus status) {
        return Attendance.builder()
            .session(session)
            .studentId(studentId)
            .status(status)
            .recordedAt(now)
            .recordedById(teacherId)
            .build();
    }
}
