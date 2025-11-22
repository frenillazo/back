package acainfo.back.user.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.user.application.ports.out.RefreshTokenRepositoryPort;
import acainfo.back.user.domain.model.RefreshTokenDomain;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.RefreshTokenJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.mappers.RefreshTokenJpaMapper;
import acainfo.back.user.infrastructure.adapters.out.persistence.mappers.UserJpaMapper;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.RefreshTokenJpaRepository;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation for RefreshTokenRepositoryPort using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public RefreshTokenDomain save(RefreshTokenDomain token) {
        RefreshTokenJpaEntity entity;

        if (token.getId() != null) {
            // Update existing token
            entity = refreshTokenJpaRepository.findById(token.getId())
                    .orElseThrow(() -> new IllegalArgumentException("RefreshToken not found with ID: " + token.getId()));
            RefreshTokenJpaMapper.updateEntity(token, entity);
        } else {
            // Create new token
            entity = RefreshTokenJpaMapper.toEntity(token);
        }

        RefreshTokenJpaEntity savedEntity = refreshTokenJpaRepository.save(entity);
        return RefreshTokenJpaMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<RefreshTokenDomain> findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token)
                .map(RefreshTokenJpaMapper::toDomain);
    }

    @Override
    public List<RefreshTokenDomain> findByUserId(Long userId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        return refreshTokenJpaRepository.findByUser(user).stream()
                .map(RefreshTokenJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefreshTokenDomain> findActiveByUserId(Long userId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        return refreshTokenJpaRepository.findByUserAndRevokedFalse(user).stream()
                .map(RefreshTokenJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeAllByUserId(Long userId) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        refreshTokenJpaRepository.revokeAllByUser(user);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens(LocalDateTime beforeDate) {
        refreshTokenJpaRepository.deleteByExpiryDateBefore(beforeDate);
    }

    @Override
    public void deleteById(Long id) {
        refreshTokenJpaRepository.deleteById(id);
    }
}
