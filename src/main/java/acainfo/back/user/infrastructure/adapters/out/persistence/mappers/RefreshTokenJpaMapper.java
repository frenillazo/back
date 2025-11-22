package acainfo.back.user.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.user.domain.model.RefreshTokenDomain;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.RefreshTokenJpaEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mapper for converting between RefreshTokenDomain and RefreshTokenJpaEntity.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RefreshTokenJpaMapper {

    /**
     * Converts RefreshTokenJpaEntity to RefreshTokenDomain.
     *
     * @param entity the JPA entity
     * @return the domain model, or null if entity is null
     */
    public static RefreshTokenDomain toDomain(RefreshTokenJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return RefreshTokenDomain.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .user(UserJpaMapper.toDomain(entity.getUser()))
                .expiryDate(entity.getExpiryDate())
                .revoked(entity.isRevoked())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Converts RefreshTokenDomain to RefreshTokenJpaEntity.
     *
     * @param domain the domain model
     * @return the JPA entity, or null if domain is null
     */
    public static RefreshTokenJpaEntity toEntity(RefreshTokenDomain domain) {
        if (domain == null) {
            return null;
        }

        return RefreshTokenJpaEntity.builder()
                .id(domain.getId())
                .token(domain.getToken())
                .user(UserJpaMapper.toEntity(domain.getUser()))
                .expiryDate(domain.getExpiryDate())
                .revoked(domain.isRevoked())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    /**
     * Updates an existing RefreshTokenJpaEntity with values from RefreshTokenDomain.
     *
     * @param domain the domain model with new values
     * @param entity the existing JPA entity to update
     */
    public static void updateEntity(RefreshTokenDomain domain, RefreshTokenJpaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setToken(domain.getToken());
        entity.setUser(UserJpaMapper.toEntity(domain.getUser()));
        entity.setExpiryDate(domain.getExpiryDate());
        entity.setRevoked(domain.isRevoked());
    }
}
