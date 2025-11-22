package acainfo.back.user.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.user.application.ports.out.RoleRepositoryPort;
import acainfo.back.user.domain.model.RoleDomain;
import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.RoleJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.mappers.RoleJpaMapper;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation for RoleRepositoryPort using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository roleJpaRepository;

    @Override
    public RoleDomain save(RoleDomain role) {
        RoleJpaEntity entity;

        if (role.getId() != null) {
            // Update existing role
            entity = roleJpaRepository.findById(role.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + role.getId()));
            RoleJpaMapper.updateEntity(role, entity);
        } else {
            // Create new role
            entity = RoleJpaMapper.toEntity(role);
        }

        RoleJpaEntity savedEntity = roleJpaRepository.save(entity);
        return RoleJpaMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<RoleDomain> findById(Long id) {
        return roleJpaRepository.findById(id)
                .map(RoleJpaMapper::toDomain);
    }

    @Override
    public Optional<RoleDomain> findByType(RoleType type) {
        return roleJpaRepository.findByType(type)
                .map(RoleJpaMapper::toDomain);
    }

    @Override
    public Optional<RoleDomain> findByName(String name) {
        return roleJpaRepository.findByName(name)
                .map(RoleJpaMapper::toDomain);
    }

    @Override
    public boolean existsByType(RoleType type) {
        return roleJpaRepository.existsByType(type);
    }

    @Override
    public boolean existsByName(String name) {
        return roleJpaRepository.existsByName(name);
    }

    @Override
    public void deleteById(Long id) {
        roleJpaRepository.deleteById(id);
    }

    @Override
    public List<RoleDomain> findAll() {
        return roleJpaRepository.findAll().stream()
                .map(RoleJpaMapper::toDomain)
                .collect(Collectors.toList());
    }
}
