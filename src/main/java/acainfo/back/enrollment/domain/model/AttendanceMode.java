package acainfo.back.enrollment.domain.model;

/**
 * Enum representing the attendance mode for an enrollment.
 * Determines how the student will attend classes for this enrollment.
 */
public enum AttendanceMode {
    /**
     * Presential attendance - student attends in-person classes
     * Requires physical space in the classroom
     */
    PRESENCIAL("Presencial"),

    /**
     * Online attendance - student attends remotely via video conferencing
     * Available for students with 2 or more active enrollments when group is full
     */
    ONLINE("Online");

    private final String displayName;

    AttendanceMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPresential() {
        return this == PRESENCIAL;
    }

    public boolean isOnline() {
        return this == ONLINE;
    }
}
