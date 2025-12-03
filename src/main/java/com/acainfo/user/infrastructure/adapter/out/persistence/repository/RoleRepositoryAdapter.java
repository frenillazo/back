package com.acainfo.user.infrastructure.adapter.out.persistence.repository;

import com.acainfo.user.application.port.out.RoleRepositoryPort;
import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.RoleJpaEntity;
import com.acainfo.user.infrastructure.mapper.RolePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing RoleRepositoryPort.
 * Translates domain operations to JPA operations.
 * Uses RolePersistenceMapper to convert between domain and JPA entities.
 */
@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final JpaRoleRepository jpaRoleRepository;
    private final RolePersistenceMapper rolePersistenceMapper;

    @Override
    public Role save(Role role) {
        RoleJpaEntity jpaEntity = rolePersistenceMapper.toJpaEntity(role);
        RoleJpaEntity savedEntity = jpaRoleRepository.save(jpaEntity);
        return rolePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return jpaRoleRepository.findById(id)
                .map(rolePersistenceMapper::toDomain);
    }

    @Override
    public Optional<Role> findByType(RoleType type) {
        return jpaRoleRepository.findByType(type)
                .map(rolePersistenceMapper::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpaRoleRepository.findAll().stream()
                .map(rolePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByType(RoleType type) {
        return jpaRoleRepository.existsByType(type);
    }
}
