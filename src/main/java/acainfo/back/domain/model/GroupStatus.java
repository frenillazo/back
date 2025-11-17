package acainfo.back.domain.model;

/**
 * Enum representing the status of a group.
 */
public enum GroupStatus {
    /**
     * Active group - accepting enrollments and running classes
     */
    ACTIVO("Activo"),

    /**
     * Inactive group - temporarily disabled, no classes running
     */
    INACTIVO("Inactivo"),

    /**
     * Full group - maximum capacity reached, no more enrollments allowed
     */
    COMPLETO("Completo"),

    /**
     * Cancelled group - group was cancelled and won't run
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
