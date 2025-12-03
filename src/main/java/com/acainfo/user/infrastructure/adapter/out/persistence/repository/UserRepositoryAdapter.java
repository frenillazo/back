package com.acainfo.user.infrastructure.adapter.out.persistence.repository;

import com.acainfo.user.application.dto.UserFilters;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.acainfo.user.infrastructure.adapter.out.persistence.specification.UserSpecifications;
import com.acainfo.user.infrastructure.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter implementing UserRepositoryPort.
 * Translates domain operations to JPA operations.
 * Uses UserPersistenceMapper to convert between domain and JPA entities.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaUserRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity = userPersistenceMapper.toJpaEntity(user);
        UserJpaEntity savedEntity = jpaUserRepository.save(jpaEntity);
        return userPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmailIgnoreCase(email)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Page<User> findWithFilters(UserFilters filters) {
        // Build specification from filters (stub implementation in Hito 1.5)
        Specification<UserJpaEntity> spec = UserSpecifications.withFilters(filters);

        // Build pagination and sorting
        Sort sort = filters.sortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageRequest = PageRequest.of(filters.page(), filters.size(), sort);

        // Execute query and map to domain
        return jpaUserRepository.findAll(spec, pageRequest)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public void delete(User user) {
        UserJpaEntity jpaEntity = userPersistenceMapper.toJpaEntity(user);
        jpaUserRepository.delete(jpaEntity);
    }

    @Override
    public void deleteById(Long id) {
        jpaUserRepository.deleteById(id);
    }
}
