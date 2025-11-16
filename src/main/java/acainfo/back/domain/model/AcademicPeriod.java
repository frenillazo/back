package acainfo.back.domain.model;

/**
 * Enum representing the academic periods for groups.
 * This determines when a group is scheduled in the academic calendar.
 */
public enum AcademicPeriod {
    /**
     * First semester (September - February)
     * Includes regular groups and intensive groups before ordinary exams
     */
    CUATRIMESTRE_1("Primer Cuatrimestre"),

    /**
     * Second semester (February - June)
     * Includes regular groups and intensive groups before ordinary exams
     */
    CUATRIMESTRE_2("Segundo Cuatrimestre"),

    /**
     * Intensive period (July - September)
     * For extraordinary exam preparation
     */
    INTENSIVO("Intensivo Extraordinaria");

    private final String displayName;

    AcademicPeriod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRegularSemester() {
        return this == CUATRIMESTRE_1 || this == CUATRIMESTRE_2;
    }

    public boolean isIntensivePeriod() {
        return this == INTENSIVO;
    }
}
