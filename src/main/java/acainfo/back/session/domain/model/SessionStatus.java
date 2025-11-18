package acainfo.back.session.domain.model;

/**
 * Estado del ciclo de vida de una sesión
 *
 * Flujo normal: PROGRAMADA → EN_CURSO → COMPLETADA
 * Flujo alternativo: PROGRAMADA → POSPUESTA → (nueva sesión PROGRAMADA)
 * Flujo excepcional: PROGRAMADA → CANCELADA
 */
public enum SessionStatus {
    /**
     * Sesión programada, pendiente de inicio
     * Estado inicial de toda sesión
     */
    PROGRAMADA,

    /**
     * Sesión en curso actualmente
     * El profesor ha iniciado la sesión
     */
    EN_CURSO,

    /**
     * Sesión finalizada correctamente
     * Se ha registrado asistencia y contenido impartido
     */
    COMPLETADA,

    /**
     * Sesión pospuesta para otra fecha
     * Se debe crear una nueva sesión de recuperación
     * Estado terminal - no se puede volver a usar esta sesión
     */
    POSPUESTA,

    /**
     * Sesión cancelada (no habrá recuperación)
     * Razones: profesor enfermo sin sustituto, festivo inesperado, etc.
     * Estado terminal - no se puede volver a usar esta sesión
     */
    CANCELADA
}
