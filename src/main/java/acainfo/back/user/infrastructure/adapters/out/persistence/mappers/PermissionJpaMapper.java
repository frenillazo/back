package acainfo.back.user.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.user.domain.model.PermissionDomain;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.PermissionJpaEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mapper for converting between PermissionDomain and PermissionJpaEntity.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PermissionJpaMapper {

    /**
     * Converts PermissionJpaEntity to PermissionDomain.
     *
     * @param entity the JPA entity
     * @return the domain model, or null if entity is null
     */
    public static PermissionDomain toDomain(PermissionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return PermissionDomain.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts PermissionDomain to PermissionJpaEntity.
     *
     * @param domain the domain model
     * @return the JPA entity, or null if domain is null
     */
    public static PermissionJpaEntity toEntity(PermissionDomain domain) {
        if (domain == null) {
            return null;
        }

        return PermissionJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing PermissionJpaEntity with values from PermissionDomain.
     *
     * @param domain the domain model with new values
     * @param entity the existing JPA entity to update
     */
    public static void updateEntity(PermissionDomain domain, PermissionJpaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
    }
}
