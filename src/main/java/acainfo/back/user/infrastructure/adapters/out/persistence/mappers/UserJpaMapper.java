package acainfo.back.user.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.user.domain.model.UserDomain;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Mapper for converting between UserDomain and UserJpaEntity.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserJpaMapper {

    /**
     * Converts UserJpaEntity to UserDomain.
     *
     * @param entity the JPA entity
     * @return the domain model, or null if entity is null
     */
    public static UserDomain toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserDomain.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .roles(entity.getRoles() != null
                        ? entity.getRoles().stream()
                                .map(RoleJpaMapper::toDomain)
                                .collect(Collectors.toSet())
                        : Collections.emptySet())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts UserDomain to UserJpaEntity.
     *
     * @param domain the domain model
     * @return the JPA entity, or null if domain is null
     */
    public static UserJpaEntity toEntity(UserDomain domain) {
        if (domain == null) {
            return null;
        }

        UserJpaEntity entity = UserJpaEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .phone(domain.getPhone())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();

        // Map roles
        if (domain.getRoles() != null) {
            var roleEntities = domain.getRoles().stream()
                    .map(RoleJpaMapper::toEntity)
                    .collect(Collectors.toSet());
            entity.setRoles(roleEntities);
        }

        return entity;
    }

    /**
     * Converts UserJpaEntity to UserDomain without roles (to avoid circular dependencies in some cases).
     *
     * @param entity the JPA entity
     * @return the domain model without roles, or null if entity is null
     */
    public static UserDomain toDomainWithoutRoles(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserDomain.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing UserJpaEntity with values from UserDomain.
     *
     * @param domain the domain model with new values
     * @param entity the existing JPA entity to update
     */
    public static void updateEntity(UserDomain domain, UserJpaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setPhone(domain.getPhone());
        entity.setStatus(domain.getStatus());

        // Update roles
        if (domain.getRoles() != null) {
            entity.getRoles().clear();
            domain.getRoles().stream()
                    .map(RoleJpaMapper::toEntity)
                    .forEach(entity::addRole);
        }
    }
}
