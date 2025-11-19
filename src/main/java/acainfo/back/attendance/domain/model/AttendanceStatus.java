package acainfo.back.attendance.domain.model;

/**
 * Estado de la asistencia de un estudiante a una sesión
 *
 * Estados posibles según las reglas de negocio del centro de formación:
 * - PRESENTE: El estudiante asistió a la sesión completa
 * - AUSENTE: El estudiante no asistió a la sesión
 * - TARDANZA: El estudiante llegó tarde pero asistió
 * - JUSTIFICADO: La ausencia fue justificada (certificado médico, etc.)
 */
public enum AttendanceStatus {
    /**
     * El estudiante asistió a la sesión completa
     * Cuenta como asistencia efectiva para estadísticas
     */
    PRESENTE,

    /**
     * El estudiante no asistió a la sesión
     * No cuenta como asistencia efectiva
     * Puede ser modificado a JUSTIFICADO posteriormente
     */
    AUSENTE,

    /**
     * El estudiante llegó tarde pero asistió
     * Cuenta como asistencia efectiva (con observación)
     * Parámetro configurable: tardanza > 15 minutos se registra como TARDANZA
     */
    TARDANZA,

    /**
     * La ausencia fue justificada con documentación
     * No cuenta como asistencia efectiva, pero no penaliza
     * Requiere aprobación del profesor o administrador
     */
    JUSTIFICADO;

    /**
     * Verifica si el estado cuenta como asistencia efectiva
     * (para cálculo de estadísticas)
     */
    public boolean isEffectiveAttendance() {
        return this == PRESENTE || this == TARDANZA;
    }

    /**
     * Verifica si el estado requiere justificación
     */
    public boolean canBeJustified() {
        return this == AUSENTE;
    }

    /**
     * Verifica si es una ausencia (justificada o no)
     */
    public boolean isAbsence() {
        return this == AUSENTE || this == JUSTIFICADO;
    }
}
