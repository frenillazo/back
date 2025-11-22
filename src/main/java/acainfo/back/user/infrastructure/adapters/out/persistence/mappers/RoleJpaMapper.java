package acainfo.back.user.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.user.domain.model.RoleDomain;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.RoleJpaEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between RoleDomain and RoleJpaEntity.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleJpaMapper {

    /**
     * Converts RoleJpaEntity to RoleDomain.
     *
     * @param entity the JPA entity
     * @return the domain model, or null if entity is null
     */
    public static RoleDomain toDomain(RoleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return RoleDomain.builder()
                .id(entity.getId())
                .type(entity.getType())
                .name(entity.getName())
                .description(entity.getDescription())
                .permissions(entity.getPermissions() != null
                        ? entity.getPermissions().stream()
                                .map(PermissionJpaMapper::toDomain)
                                .collect(Collectors.toSet())
                        : Collections.emptySet())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts RoleDomain to RoleJpaEntity.
     *
     * @param domain the domain model
     * @return the JPA entity, or null if domain is null
     */
    public static RoleJpaEntity toEntity(RoleDomain domain) {
        if (domain == null) {
            return null;
        }

        RoleJpaEntity entity = RoleJpaEntity.builder()
                .id(domain.getId())
                .type(domain.getType())
                .name(domain.getName())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        // Map permissions
        if (domain.getPermissions() != null) {
            Set<acainfo.back.user.infrastructure.adapters.out.persistence.entities.PermissionJpaEntity> permissionEntities =
                    domain.getPermissions().stream()
                            .map(PermissionJpaMapper::toEntity)
                            .collect(Collectors.toSet());
            entity.setPermissions(permissionEntities);
        }

        return entity;
    }

    /**
     * Converts RoleJpaEntity to RoleDomain without permissions (to avoid circular dependencies).
     *
     * @param entity the JPA entity
     * @return the domain model without permissions, or null if entity is null
     */
    public static RoleDomain toDomainWithoutPermissions(RoleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return RoleDomain.builder()
                .id(entity.getId())
                .type(entity.getType())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing RoleJpaEntity with values from RoleDomain.
     *
     * @param domain the domain model with new values
     * @param entity the existing JPA entity to update
     */
    public static void updateEntity(RoleDomain domain, RoleJpaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setType(domain.getType());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());

        // Update permissions
        if (domain.getPermissions() != null) {
            entity.getPermissions().clear();
            domain.getPermissions().stream()
                    .map(PermissionJpaMapper::toEntity)
                    .forEach(entity::addPermission);
        }
    }
}
