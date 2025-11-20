package acainfo.back.enrollment.domain.model;

/**
 * Enum representing the status of an enrollment.
 */
public enum EnrollmentStatus {
    /**
     * Active enrollment - student is enrolled and attending
     */
    ACTIVO("Activo"),

    /**
     * Withdrawn enrollment - student has withdrawn from the group
     */
    RETIRADO("Retirado"),

    /**
     * Waiting enrollment - student is in waiting queue (group is full)
     */
    EN_ESPERA("En Espera");

    private final String displayName;

    EnrollmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == ACTIVO;
    }

    public boolean isWithdrawn() {
        return this == RETIRADO;
    }

    public boolean isWaiting() {
        return this == EN_ESPERA;
    }
}
