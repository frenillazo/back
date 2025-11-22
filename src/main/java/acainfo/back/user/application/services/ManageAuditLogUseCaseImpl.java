package acainfo.back.user.application.services;

import acainfo.back.user.application.ports.in.ManageAuditLogUseCase;
import acainfo.back.user.application.ports.out.AuditLogRepositoryPort;
import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.domain.model.AuditAction;
import acainfo.back.user.domain.model.AuditLogDomain;
import acainfo.back.user.domain.model.UserDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ManageAuditLogUseCase.
 * Handles all audit logging operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManageAuditLogUseCaseImpl implements ManageAuditLogUseCase {

    private final AuditLogRepositoryPort auditLogRepository;
    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public void log(Long userId, AuditAction action, String details) {
        UserDomain user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElse(null); // Don't fail if user not found for audit logs
        }

        AuditLogDomain auditLog = AuditLogDomain.builder()
                .user(user)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log created: action={}, userId={}", action, userId);
    }

    @Override
    @Transactional
    public void logWithEntity(Long userId, AuditAction action, String entityType, Long entityId, String details) {
        UserDomain user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElse(null);
        }

        AuditLogDomain auditLog = AuditLogDomain.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log created: action={}, userId={}, entity={}:{}", action, userId, entityType, entityId);
    }

    @Override
    @Transactional
    public void logFailedLogin(String email, String reason) {
        AuditLogDomain auditLog = AuditLogDomain.builder()
                .user(null) // No user for failed login
                .action(AuditAction.LOGIN_FAILED)
                .details("Email: " + email + ", Reason: " + reason)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Failed login attempt logged: email={}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDomain> getAuditLogsByUser(Long userId, Pageable pageable) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new UserNotFoundException(userId);
        }

        return auditLogRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDomain> getFailedLoginAttempts(Long userId, LocalDateTime since) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new UserNotFoundException(userId);
        }

        return auditLogRepository.findFailedLoginAttempts(userId, since);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDomain> getRecentAuditLogs(Pageable pageable) {
        return auditLogRepository.findRecentLogs(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDomain> getAuditLogsByAction(AuditAction action) {
        return auditLogRepository.findByAction(action);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDomain> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntity(entityType, entityId);
    }

    @Override
    @Transactional
    public void cleanupOldLogs(LocalDateTime olderThan) {
        auditLogRepository.deleteOldLogs(olderThan);
        log.info("Cleaned up audit logs older than {}", olderThan);
    }
}
