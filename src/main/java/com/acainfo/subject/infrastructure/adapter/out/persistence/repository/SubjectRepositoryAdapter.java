package com.acainfo.subject.infrastructure.adapter.out.persistence.repository;

import com.acainfo.subject.application.dto.SubjectFilters;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.subject.infrastructure.adapter.out.persistence.entity.SubjectJpaEntity;
import com.acainfo.subject.infrastructure.adapter.out.persistence.specification.SubjectSpecifications;
import com.acainfo.subject.infrastructure.mapper.SubjectPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter implementing SubjectRepositoryPort.
 * Translates domain operations to JPA operations.
 * Uses SubjectPersistenceMapper to convert between domain and JPA entities.
 */
@Component
@RequiredArgsConstructor
public class SubjectRepositoryAdapter implements SubjectRepositoryPort {

    private final JpaSubjectRepository jpaSubjectRepository;
    private final SubjectPersistenceMapper subjectPersistenceMapper;

    @Override
    public Subject save(Subject subject) {
        SubjectJpaEntity jpaEntity = subjectPersistenceMapper.toJpaEntity(subject);
        SubjectJpaEntity savedEntity = jpaSubjectRepository.save(jpaEntity);
        return subjectPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Subject> findById(Long id) {
        return jpaSubjectRepository.findById(id)
                .map(subjectPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Subject> findByCode(String code) {
        return jpaSubjectRepository.findByCodeIgnoreCase(code)
                .map(subjectPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaSubjectRepository.existsByCodeIgnoreCase(code);
    }

    @Override
    public Page<Subject> findWithFilters(SubjectFilters filters) {
        // Build specification from filters
        Specification<SubjectJpaEntity> spec = SubjectSpecifications.withFilters(filters);

        // Build pagination and sorting
        Sort sort = filters.sortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageRequest = PageRequest.of(filters.page(), filters.size(), sort);

        // Execute query and map to domain
        return jpaSubjectRepository.findAll(spec, pageRequest)
                .map(subjectPersistenceMapper::toDomain);
    }

    @Override
    public void delete(Long id) {
        jpaSubjectRepository.deleteById(id);
    }
}
