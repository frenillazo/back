package acainfo.back.session.domain.model;

/**
 * Modalidad de una sesión de clase
 *
 * - PRESENCIAL: Sesión completamente presencial en el aula física
 * - DUAL: Sesión híbrida (presencial + online simultáneamente)
 * - ONLINE: Sesión completamente online (videoconferencia)
 */
public enum SessionMode {
    /**
     * Sesión completamente presencial en aula física
     * Requiere: Classroom asignado
     */
    PRESENCIAL,

    /**
     * Sesión híbrida (estudiantes presenciales y online simultáneamente)
     * Requiere: Classroom asignado + Zoom meeting ID
     */
    DUAL,

    /**
     * Sesión completamente online vía videoconferencia
     * Requiere: Zoom meeting ID
     * No requiere: Classroom físico
     */
    ONLINE;

    /**
     * Checks if this mode requires a physical classroom
     */
    public boolean requiresPhysicalClassroom() {
        return this == PRESENCIAL || this == DUAL;
    }

    /**
     * Checks if this mode requires Zoom meeting
     */
    public boolean requiresZoom() {
        return this == ONLINE || this == DUAL;
    }

    /**
     * Checks if this is presencial mode
     */
    public boolean isPresencial() {
        return this == PRESENCIAL;
    }

    /**
     * Checks if this is dual mode
     */
    public boolean isDual() {
        return this == DUAL;
    }

    /**
     * Checks if this is online mode
     */
    public boolean isOnline() {
        return this == ONLINE;
    }
}
