package acainfo.back.user.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.user.application.ports.out.PermissionRepositoryPort;
import acainfo.back.user.domain.model.PermissionDomain;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.PermissionJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.mappers.PermissionJpaMapper;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.PermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation for PermissionRepositoryPort using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class PermissionRepositoryAdapter implements PermissionRepositoryPort {

    private final PermissionJpaRepository permissionJpaRepository;

    @Override
    public PermissionDomain save(PermissionDomain permission) {
        PermissionJpaEntity entity;

        if (permission.getId() != null) {
            // Update existing permission
            entity = permissionJpaRepository.findById(permission.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + permission.getId()));
            PermissionJpaMapper.updateEntity(permission, entity);
        } else {
            // Create new permission
            entity = PermissionJpaMapper.toEntity(permission);
        }

        PermissionJpaEntity savedEntity = permissionJpaRepository.save(entity);
        return PermissionJpaMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PermissionDomain> findById(Long id) {
        return permissionJpaRepository.findById(id)
                .map(PermissionJpaMapper::toDomain);
    }

    @Override
    public Optional<PermissionDomain> findByName(String name) {
        return permissionJpaRepository.findByName(name)
                .map(PermissionJpaMapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return permissionJpaRepository.existsByName(name);
    }

    @Override
    public void deleteById(Long id) {
        permissionJpaRepository.deleteById(id);
    }

    @Override
    public List<PermissionDomain> findAll() {
        return permissionJpaRepository.findAll().stream()
                .map(PermissionJpaMapper::toDomain)
                .collect(Collectors.toList());
    }
}
