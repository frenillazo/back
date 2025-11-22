package acainfo.back.user.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.user.domain.model.AuditLogDomain;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.AuditLogJpaEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mapper for converting between AuditLogDomain and AuditLogJpaEntity.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuditLogJpaMapper {

    /**
     * Converts AuditLogJpaEntity to AuditLogDomain.
     *
     * @param entity the JPA entity
     * @return the domain model, or null if entity is null
     */
    public static AuditLogDomain toDomain(AuditLogJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return AuditLogDomain.builder()
                .id(entity.getId())
                .user(UserJpaMapper.toDomain(entity.getUser()))
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .details(entity.getDetails())
                .ipAddress(entity.getIpAddress())
                .timestamp(entity.getTimestamp())
                .build();
    }

    /**
     * Converts AuditLogDomain to AuditLogJpaEntity.
     *
     * @param domain the domain model
     * @return the JPA entity, or null if domain is null
     */
    public static AuditLogJpaEntity toEntity(AuditLogDomain domain) {
        if (domain == null) {
            return null;
        }

        return AuditLogJpaEntity.builder()
                .id(domain.getId())
                .user(UserJpaMapper.toEntity(domain.getUser()))
                .action(domain.getAction())
                .entityType(domain.getEntityType())
                .entityId(domain.getEntityId())
                .details(domain.getDetails())
                .ipAddress(domain.getIpAddress())
                .timestamp(domain.getTimestamp())
                .build();
    }

    /**
     * Converts AuditLogJpaEntity to AuditLogDomain without full user details.
     * Only includes user ID to avoid loading full user graph.
     *
     * @param entity the JPA entity
     * @return the domain model with minimal user data, or null if entity is null
     */
    public static AuditLogDomain toDomainWithMinimalUser(AuditLogJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return AuditLogDomain.builder()
                .id(entity.getId())
                .user(entity.getUser() != null ? UserJpaMapper.toDomainWithoutRoles(entity.getUser()) : null)
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .details(entity.getDetails())
                .ipAddress(entity.getIpAddress())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
