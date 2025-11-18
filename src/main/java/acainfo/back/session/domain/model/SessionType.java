package acainfo.back.session.domain.model;

/**
 * Tipo de sesión según su naturaleza
 *
 * Determina el origen y propósito de la sesión
 */
public enum SessionType {
    /**
     * Sesión regular del calendario académico
     * Programada según el horario normal del grupo
     */
    REGULAR,

    /**
     * Sesión de recuperación
     * Creada para compensar una sesión POSPUESTA o CANCELADA
     * Referencia a la sesión original mediante recoveryForSessionId
     */
    RECUPERACION,

    /**
     * Sesión extra
     * Programada adicionalmente (ej: repasos, tutorías grupales, etc.)
     * No forma parte del calendario regular
     */
    EXTRA
}
