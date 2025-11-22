package acainfo.back.user.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.domain.model.UserDomain;
import acainfo.back.user.domain.model.UserStatus;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.mappers.UserJpaMapper;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation for UserRepositoryPort using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public UserDomain save(UserDomain user) {
        UserJpaEntity entity;

        if (user.getId() != null) {
            // Update existing user
            entity = userJpaRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));
            UserJpaMapper.updateEntity(user, entity);
        } else {
            // Create new user
            entity = UserJpaMapper.toEntity(user);
        }

        UserJpaEntity savedEntity = userJpaRepository.save(entity);
        return UserJpaMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<UserDomain> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<UserDomain> findByEmailIgnoreCase(String email) {
        return userJpaRepository.findByEmailIgnoreCase(email)
                .map(UserJpaMapper::toDomain);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return userJpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public List<UserDomain> findByStatus(UserStatus status) {
        return userJpaRepository.findByStatus(status).stream()
                .map(UserJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDomain> findByRoleType(RoleType roleType) {
        return userJpaRepository.findByRoleType(roleType).stream()
                .map(UserJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDomain> findByRoleTypeAndStatus(RoleType roleType, UserStatus status) {
        return userJpaRepository.findByRoleTypeAndStatus(roleType, status).stream()
                .map(UserJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDomain> searchByName(String searchTerm) {
        return userJpaRepository.searchByName(searchTerm).stream()
                .map(UserJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public List<UserDomain> findAll() {
        return userJpaRepository.findAll().stream()
                .map(UserJpaMapper::toDomain)
                .collect(Collectors.toList());
    }
}
