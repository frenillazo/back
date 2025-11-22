package acainfo.back.user.application.ports.in;

import acainfo.back.user.domain.model.AuditAction;
import acainfo.back.user.domain.model.AuditLogDomain;
import acainfo.back.user.domain.model.UserDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use case port for audit log management operations.
 * Defines the contract for audit logging business logic.
 */
public interface ManageAuditLogUseCase {

    /**
     * Logs an audit event.
     *
     * @param userId the user ID (nullable for anonymous actions)
     * @param action the audit action
     * @param details optional details
     */
    void log(Long userId, AuditAction action, String details);

    /**
     * Logs an audit event with entity information.
     *
     * @param userId the user ID
     * @param action the audit action
     * @param entityType the entity type
     * @param entityId the entity ID
     * @param details optional details
     */
    void logWithEntity(Long userId, AuditAction action, String entityType, Long entityId, String details);

    /**
     * Logs a failed login attempt.
     *
     * @param email the email used in the attempt
     * @param reason the failure reason
     */
    void logFailedLogin(String email, String reason);

    /**
     * Gets audit logs for a user (paginated).
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<AuditLogDomain> getAuditLogsByUser(Long userId, Pageable pageable);

    /**
     * Gets failed login attempts for a user since a specific time.
     *
     * @param userId the user ID
     * @param since datetime threshold
     * @return list of failed login attempts
     */
    List<AuditLogDomain> getFailedLoginAttempts(Long userId, LocalDateTime since);

    /**
     * Gets recent audit logs (paginated).
     *
     * @param pageable pagination information
     * @return page of recent audit logs
     */
    Page<AuditLogDomain> getRecentAuditLogs(Pageable pageable);

    /**
     * Gets audit logs by action type.
     *
     * @param action the audit action
     * @return list of audit logs
     */
    List<AuditLogDomain> getAuditLogsByAction(AuditAction action);

    /**
     * Gets audit logs for an entity.
     *
     * @param entityType the entity type
     * @param entityId the entity ID
     * @return list of audit logs
     */
    List<AuditLogDomain> getAuditLogsByEntity(String entityType, Long entityId);

    /**
     * Cleans up old audit logs.
     *
     * @param olderThan cutoff date
     */
    void cleanupOldLogs(LocalDateTime olderThan);
}
