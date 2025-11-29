package acainfo.back.user.application.services;

import acainfo.back.user.domain.model.AuditAction;
import acainfo.back.user.domain.model.AuditLog;
import acainfo.back.user.domain.model.User;
import acainfo.back.user.infrastructure.adapters.out.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an audit action asynchronously
     */
    @Async
    @Transactional
    public void log(User user, AuditAction action, String details) {
        try {
            String ipAddress = getClientIpAddress();

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .details(details)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {}", action, user.getEmail());
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log an audit action with entity information
     */
    @Async
    @Transactional
    public void log(User user, AuditAction action, String entityType, Long entityId, String details) {
        try {
            String ipAddress = getClientIpAddress();

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {}/{}", action, user.getEmail(), entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a failed login attempt
     */
    @Async
    @Transactional
    public void logFailedLogin(String email, String reason) {
        try {
            String ipAddress = getClientIpAddress();

            AuditLog auditLog = AuditLog.builder()
                    .user(null) // No user for failed login
                    .action(AuditAction.LOGIN_FAILED)
                    .details("Email: " + email + " - Reason: " + reason)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Failed login attempt logged for: {}", email);
        } catch (Exception e) {
            log.error("Failed to create audit log for failed login: {}", e.getMessage(), e);
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Failed to get client IP address: {}", e.getMessage());
        }
        return "unknown";
    }
}
