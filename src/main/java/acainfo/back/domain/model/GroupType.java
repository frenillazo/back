package acainfo.back.domain.model;

/**
 * Enum representing the type of a group.
 * Determines the format and intensity of the group.
 */
public enum GroupType {
    /**
     * Regular group - Standard semester-long course
     */
    REGULAR("Regular"),

    /**
     * Intensive group - Condensed format for exam preparation
     * Can be pre-ordinary exam or extraordinary exam
     */
    INTENSIVO("Intensivo");

    private final String displayName;

    GroupType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRegular() {
        return this == REGULAR;
    }

    public boolean isIntensive() {
        return this == INTENSIVO;
    }
}
