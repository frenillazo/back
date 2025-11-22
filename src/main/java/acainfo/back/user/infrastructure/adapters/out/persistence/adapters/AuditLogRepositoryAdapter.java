package acainfo.back.user.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.user.application.ports.out.AuditLogRepositoryPort;
import acainfo.back.user.domain.model.AuditAction;
import acainfo.back.user.domain.model.AuditLogDomain;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.AuditLogJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.mappers.AuditLogJpaMapper;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.AuditLogJpaRepository;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation for AuditLogRepositoryPort using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final AuditLogJpaRepository auditLogJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public AuditLogDomain save(AuditLogDomain auditLog) {
        AuditLogJpaEntity entity = AuditLogJpaMapper.toEntity(auditLog);
        AuditLogJpaEntity savedEntity = auditLogJpaRepository.save(entity);
        return AuditLogJpaMapper.toDomain(savedEntity);
    }

    @Override
    public Page<AuditLogDomain> findByUserId(Long userId, Pageable pageable) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        return auditLogJpaRepository.findByUser(user, pageable)
                .map(AuditLogJpaMapper::toDomainWithMinimalUser);
    }

    @Override
    public List<AuditLogDomain> findFailedLoginAttempts(Long userId, LocalDateTime since) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        return auditLogJpaRepository.findByUserAndActionAndTimestampAfter(
                        user,
                        AuditAction.LOGIN_FAILED,
                        since
                ).stream()
                .map(AuditLogJpaMapper::toDomainWithMinimalUser)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AuditLogDomain> findRecentLogs(Pageable pageable) {
        return auditLogJpaRepository.findAllByOrderByTimestampDesc(pageable)
                .map(AuditLogJpaMapper::toDomainWithMinimalUser);
    }

    @Override
    public List<AuditLogDomain> findByAction(AuditAction action) {
        return auditLogJpaRepository.findByAction(action).stream()
                .map(AuditLogJpaMapper::toDomainWithMinimalUser)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDomain> findByEntity(String entityType, Long entityId) {
        return auditLogJpaRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(AuditLogJpaMapper::toDomainWithMinimalUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteOldLogs(LocalDateTime olderThan) {
        auditLogJpaRepository.deleteByTimestampBefore(olderThan);
    }

    @Override
    public Optional<AuditLogDomain> findById(Long id) {
        return auditLogJpaRepository.findById(id)
                .map(AuditLogJpaMapper::toDomain);
    }
}
