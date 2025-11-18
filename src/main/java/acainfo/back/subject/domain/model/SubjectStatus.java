package acainfo.back.subject.domain.model;

/**
 * Enum representing the status of a subject.
 */
public enum SubjectStatus {
    /**
     * Active subject - can have groups and enrollments
     */
    ACTIVO("Activo"),

    /**
     * Inactive subject - temporarily disabled, no new groups allowed
     */
    INACTIVO("Inactivo"),

    /**
     * Archived subject - historical data, no longer offered
     */
    ARCHIVADO("Archivado");

    private final String displayName;

    SubjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == ACTIVO;
    }
}
