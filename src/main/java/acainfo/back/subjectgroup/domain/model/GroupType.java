package acainfo.back.subjectgroup.domain.model;

/**
 * Enum representing the type of a subjectGroup.
 * Determines the format and intensity of the subjectGroup.
 */
public enum GroupType {
    /**
     * Regular subjectGroup - Standard semester-long course
     */
    REGULAR("Regular"),

    /**
     * Intensive subjectGroup - Condensed format for exam preparation
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
