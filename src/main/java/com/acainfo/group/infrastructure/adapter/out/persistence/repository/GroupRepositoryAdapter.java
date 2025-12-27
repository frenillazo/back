package com.acainfo.group.infrastructure.adapter.out.persistence.repository;

import com.acainfo.group.application.dto.GroupFilters;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.group.infrastructure.adapter.out.persistence.entity.SubjectGroupJpaEntity;
import com.acainfo.group.infrastructure.adapter.out.persistence.specification.GroupSpecifications;
import com.acainfo.group.infrastructure.mapper.GroupPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing GroupRepositoryPort.
 * Translates domain operations to JPA operations.
 * Uses GroupPersistenceMapper to convert between domain and JPA entities.
 */
@Component
@RequiredArgsConstructor
public class GroupRepositoryAdapter implements GroupRepositoryPort {

    private final JpaGroupRepository jpaGroupRepository;
    private final GroupPersistenceMapper groupPersistenceMapper;

    @Override
    public SubjectGroup save(SubjectGroup group) {
        SubjectGroupJpaEntity jpaEntity = groupPersistenceMapper.toJpaEntity(group);
        SubjectGroupJpaEntity savedEntity = jpaGroupRepository.save(jpaEntity);
        return groupPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SubjectGroup> findById(Long id) {
        return jpaGroupRepository.findById(id)
                .map(groupPersistenceMapper::toDomain);
    }

    @Override
    public Page<SubjectGroup> findWithFilters(GroupFilters filters) {
        // Build specification from filters
        Specification<SubjectGroupJpaEntity> spec = GroupSpecifications.withFilters(filters);

        // Build pagination and sorting
        Sort sort = filters.sortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageRequest = PageRequest.of(filters.page(), filters.size(), sort);

        // Execute query and map to domain
        return jpaGroupRepository.findAll(spec, pageRequest)
                .map(groupPersistenceMapper::toDomain);
    }

    @Override
    public void delete(Long id) {
        jpaGroupRepository.deleteById(id);
    }

    @Override
    public List<SubjectGroup> findAll() {
        return jpaGroupRepository.findAll().stream()
                .map(groupPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<SubjectGroup> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaGroupRepository.findAllById(ids).stream()
                .map(groupPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countActiveGroupsByTeacherId(Long teacherId) {
        return jpaGroupRepository.countByTeacherIdAndStatusIn(
                teacherId,
                List.of(GroupStatus.OPEN, GroupStatus.CLOSED)
        );
    }

    @Override
    public long countActiveGroupsBySubjectId(Long subjectId) {
        return jpaGroupRepository.countBySubjectIdAndStatusIn(
                subjectId,
                List.of(GroupStatus.OPEN, GroupStatus.CLOSED)
        );
    }
}
