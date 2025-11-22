package acainfo.back.user.application.ports.out;

import acainfo.back.user.domain.model.AuditAction;
import acainfo.back.user.domain.model.AuditLogDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Port (interface) for AuditLog repository operations.
 * Defines the contract for audit log persistence.
 * Works with AuditLogDomain (pure domain model).
 */
public interface AuditLogRepositoryPort {

    /**
     * Saves an audit log entry.
     *
     * @param auditLog the audit log to save
     * @return the saved audit log
     */
    AuditLogDomain save(AuditLogDomain auditLog);

    /**
     * Finds an audit log by ID.
     *
     * @param id the audit log ID
     * @return Optional containing the audit log if found
     */
    Optional<AuditLogDomain> findById(Long id);

    /**
     * Finds all audit logs for a specific user (paginated).
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<AuditLogDomain> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    /**
     * Finds all audit logs for a specific user (non-paginated).
     *
     * @param userId the user ID
     * @return list of audit logs
     */
    List<AuditLogDomain> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Finds audit logs by action type.
     *
     * @param action the audit action
     * @return list of audit logs
     */
    List<AuditLogDomain> findByActionOrderByTimestampDesc(AuditAction action);

    /**
     * Finds audit logs by user and action.
     *
     * @param userId the user ID
     * @param action the audit action
     * @return list of audit logs
     */
    List<AuditLogDomain> findByUserIdAndActionOrderByTimestampDesc(Long userId, AuditAction action);

    /**
     * Finds audit logs within a date range.
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of audit logs
     */
    List<AuditLogDomain> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds audit logs by entity type and entity ID.
     *
     * @param entityType the entity type
     * @param entityId the entity ID
     * @return list of audit logs
     */
    List<AuditLogDomain> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);

    /**
     * Finds recent audit logs (paginated).
     *
     * @param pageable pagination information
     * @return page of recent audit logs
     */
    Page<AuditLogDomain> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Finds failed login attempts for a user since a specific time.
     *
     * @param userId the user ID
     * @param since datetime threshold
     * @return list of failed login attempts
     */
    List<AuditLogDomain> findFailedLoginAttempts(Long userId, LocalDateTime since);

    /**
     * Counts actions by user in date range.
     *
     * @param userId the user ID
     * @param action the audit action
     * @param startDate start of date range
     * @param endDate end of date range
     * @return count of actions
     */
    long countByUserIdAndActionInDateRange(Long userId, AuditAction action,
                                            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds audit logs by IP address.
     *
     * @param ipAddress the IP address
     * @return list of audit logs
     */
    List<AuditLogDomain> findByIpAddressOrderByTimestampDesc(String ipAddress);

    /**
     * Deletes old audit logs (for cleanup).
     *
     * @param date cutoff date
     */
    void deleteByTimestampBefore(LocalDateTime date);

    /**
     * Finds all audit logs.
     *
     * @return list of all audit logs
     */
    List<AuditLogDomain> findAll();
}
