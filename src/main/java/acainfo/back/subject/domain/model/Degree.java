package acainfo.back.subject.domain.model;

/**
 * Enum representing the engineering degrees offered by the training center.
 */
public enum Degree {
    /**
     * Industrial Engineering degree
     */
    INDUSTRIAL("Ingeniería Industrial"),

    /**
     * Computer Engineering degree
     */
    INFORMATICA("Ingeniería Informática");

    private final String displayName;

    Degree(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
