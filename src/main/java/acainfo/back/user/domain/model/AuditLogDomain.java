package acainfo.back.user.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.time.LocalDateTime;

/**
 * AuditLog domain model.
 * Pure POJO - NO infrastructure dependencies, NO JPA annotations.
 * Represents an audit log entry for tracking user actions.
 */
@Value
@Builder(toBuilder = true)
public class AuditLogDomain {

    Long id;

    Long userId; // Reference to user by ID only (nullable for anonymous actions)

    @With
    AuditAction action;

    @With
    String entityType;

    @With
    Long entityId;

    @With
    String details;

    @With
    String ipAddress;

    LocalDateTime timestamp;

    /**
     * Business rule validation.
     */
    public void validate() {
        if (action == null) {
            throw new IllegalArgumentException("Action is required");
        }
        if (entityType != null && entityType.length() > 100) {
            throw new IllegalArgumentException("Entity type must not exceed 100 characters");
        }
        if (ipAddress != null && ipAddress.length() > 45) {
            throw new IllegalArgumentException("IP address must not exceed 45 characters");
        }
    }

    /**
     * Checks if this audit log is for a specific user.
     */
    public boolean isForUser(Long checkUserId) {
        return userId != null && userId.equals(checkUserId);
    }

    /**
     * Checks if this audit log is for a specific action.
     */
    public boolean isAction(AuditAction checkAction) {
        return action == checkAction;
    }

    /**
     * Checks if this is a login attempt.
     */
    public boolean isLoginAttempt() {
        return action == AuditAction.LOGIN || action == AuditAction.LOGIN_FAILED;
    }

    /**
     * Checks if this is a failed login.
     */
    public boolean isFailedLogin() {
        return action == AuditAction.LOGIN_FAILED;
    }

    /**
     * Checks if this is a successful login.
     */
    public boolean isSuccessfulLogin() {
        return action == AuditAction.LOGIN;
    }

    /**
     * Checks if this audit log has associated entity information.
     */
    public boolean hasEntityInfo() {
        return entityType != null && entityId != null;
    }
}
