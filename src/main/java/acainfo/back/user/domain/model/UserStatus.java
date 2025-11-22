package acainfo.back.user.domain.model;

/**
 * Enum representing different user status states.
 * Domain model - NO infrastructure dependencies.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING_VERIFICATION
}
