package acainfo.back.subjectgroup.domain.model;

/**
 * Enum representing the status of a subjectGroup.
 */
public enum GroupStatus {
    /**
     * Active subjectGroup - accepting enrollments and running classes
     */
    ACTIVO("Activo"),

    /**
     * Inactive subjectGroup - temporarily disabled, no classes running
     */
    INACTIVO("Inactivo"),

    /**
     * Full subjectGroup - maximum capacity reached, no more enrollments allowed
     */
    COMPLETO("Completo"),

    /**
     * Cancelled subjectGroup - subjectGroup was cancelled and won't run
     */
    CANCELADO("Cancelado");

    private final String displayName;

    GroupStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == ACTIVO;
    }

    public boolean canEnroll() {
        return this == ACTIVO;
    }

    public boolean isFull() {
        return this == COMPLETO;
    }

    public boolean isCancelled() {
        return this == CANCELADO;
    }
}
