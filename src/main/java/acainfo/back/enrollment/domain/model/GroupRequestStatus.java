package acainfo.back.enrollment.domain.model;

/**
 * Enum representing the status of a group request.
 * Students can request creation of new groups if they gather minimum supporters (8 students).
 */
public enum GroupRequestStatus {
    /**
     * Pending approval - request is gathering supporters or waiting for admin review
     */
    PENDIENTE("Pendiente"),

    /**
     * Approved - admin has approved the request, new group will be created
     */
    APROBADA("Aprobada"),

    /**
     * Rejected - admin has rejected the request
     */
    RECHAZADA("Rechazada");

    private final String displayName;

    GroupRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPending() {
        return this == PENDIENTE;
    }

    public boolean isApproved() {
        return this == APROBADA;
    }

    public boolean isRejected() {
        return this == RECHAZADA;
    }

    public boolean isResolved() {
        return this == APROBADA || this == RECHAZADA;
    }
}
