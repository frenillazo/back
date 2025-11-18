package acainfo.back.schedule.domain.model;

/**
 * Enum representing the available classrooms in the training center.
 * Each classroom has a fixed capacity.
 */
public enum Classroom {
    /**
     * Physical classroom 1 - 24 seats
     */
    AULA_1("Aula 1", 24, true),

    /**
     * Physical classroom 2 - 24 seats
     */
    AULA_2("Aula 2", 24, true),

    /**
     * Virtual classroom - unlimited capacity
     */
    VIRTUAL("Aula Virtual", 999, false);

    private final String displayName;
    private final int maxCapacity;
    private final boolean isPhysical;

    Classroom(String displayName, int maxCapacity, boolean isPhysical) {
        this.displayName = displayName;
        this.maxCapacity = maxCapacity;
        this.isPhysical = isPhysical;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public boolean isPhysical() {
        return isPhysical;
    }

    public boolean isVirtual() {
        return !isPhysical;
    }

    /**
     * Checks if this classroom has limited capacity
     */
    public boolean hasLimitedCapacity() {
        return isPhysical;
    }
}
