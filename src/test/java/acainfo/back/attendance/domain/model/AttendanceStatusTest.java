package acainfo.back.attendance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AttendanceStatus enum
 */
@DisplayName("AttendanceStatus Enum Tests")
class AttendanceStatusTest {

    @Test
    @DisplayName("PRESENTE should count as effective attendance")
    void presente_shouldCountAsEffectiveAttendance() {
        // When
        boolean result = AttendanceStatus.PRESENTE.isEffectiveAttendance();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("TARDANZA should count as effective attendance")
    void tardanza_shouldCountAsEffectiveAttendance() {
        // When
        boolean result = AttendanceStatus.TARDANZA.isEffectiveAttendance();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("AUSENTE should NOT count as effective attendance")
    void ausente_shouldNotCountAsEffectiveAttendance() {
        // When
        boolean result = AttendanceStatus.AUSENTE.isEffectiveAttendance();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("JUSTIFICADO should NOT count as effective attendance")
    void justificado_shouldNotCountAsEffectiveAttendance() {
        // When
        boolean result = AttendanceStatus.JUSTIFICADO.isEffectiveAttendance();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Only AUSENTE can be justified")
    void onlyAusente_canBeJustified() {
        // Then
        assertThat(AttendanceStatus.AUSENTE.canBeJustified()).isTrue();
        assertThat(AttendanceStatus.PRESENTE.canBeJustified()).isFalse();
        assertThat(AttendanceStatus.TARDANZA.canBeJustified()).isFalse();
        assertThat(AttendanceStatus.JUSTIFICADO.canBeJustified()).isFalse();
    }

    @Test
    @DisplayName("AUSENTE and JUSTIFICADO are absences")
    void ausenteAndJustificado_areAbsences() {
        // Then
        assertThat(AttendanceStatus.AUSENTE.isAbsence()).isTrue();
        assertThat(AttendanceStatus.JUSTIFICADO.isAbsence()).isTrue();
        assertThat(AttendanceStatus.PRESENTE.isAbsence()).isFalse();
        assertThat(AttendanceStatus.TARDANZA.isAbsence()).isFalse();
    }

    @Test
    @DisplayName("All enum values should be defined")
    void allEnumValues_shouldBeDefined() {
        // When
        AttendanceStatus[] values = AttendanceStatus.values();

        // Then
        assertThat(values).hasSize(4);
        assertThat(values).containsExactlyInAnyOrder(
            AttendanceStatus.PRESENTE,
            AttendanceStatus.AUSENTE,
            AttendanceStatus.TARDANZA,
            AttendanceStatus.JUSTIFICADO
        );
    }
}
