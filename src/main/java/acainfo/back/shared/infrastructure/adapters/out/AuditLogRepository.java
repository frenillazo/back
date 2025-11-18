package acainfo.back.shared.infrastructure.adapters.out;

import acainfo.back.shared.domain.model.AuditAction;
import acainfo.back.shared.domain.model.AuditLog;
import acainfo.back.shared.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a specific user (paginated)
     */
    Page<AuditLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);

    /**
     * Find all audit logs for a specific user (non-paginated)
     */
    List<AuditLog> findByUserOrderByTimestampDesc(User user);

    /**
     * Find audit logs by action type
     */
    List<AuditLog> findByActionOrderByTimestampDesc(AuditAction action);

    /**
     * Find audit logs by user and action
     */
    List<AuditLog> findByUserAndActionOrderByTimestampDesc(User user, AuditAction action);

    /**
     * Find audit logs within a date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs by entity type and entity id
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);

    /**
     * Find recent audit logs (paginated)
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * Find failed login attempts for a user in a time range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.user = :user " +
           "AND a.action = 'LOGIN_FAILED' " +
           "AND a.timestamp >= :since " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedLoginAttempts(@Param("user") User user,
                                          @Param("since") LocalDateTime since);

    /**
     * Count actions by user in date range
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user = :user " +
           "AND a.action = :action " +
           "AND a.timestamp BETWEEN :startDate AND :endDate")
    long countByUserAndActionInDateRange(@Param("user") User user,
                                        @Param("action") AuditAction action,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs by IP address
     */
    List<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress);

    /**
     * Delete old audit logs (for cleanup)
     */
    void deleteByTimestampBefore(LocalDateTime date);
}
