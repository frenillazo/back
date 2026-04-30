package com.acainfo.intensive.infrastructure.adapter.out.persistence.repository;

import com.acainfo.intensive.application.dto.IntensiveFilters;
import com.acainfo.intensive.application.port.out.IntensiveRepositoryPort;
import com.acainfo.intensive.domain.model.Intensive;
import com.acainfo.intensive.domain.model.IntensiveStatus;
import com.acainfo.intensive.infrastructure.adapter.out.persistence.entity.IntensiveJpaEntity;
import com.acainfo.intensive.infrastructure.adapter.out.persistence.specification.IntensiveSpecifications;
import com.acainfo.intensive.infrastructure.mapper.IntensivePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing {@link IntensiveRepositoryPort} on top of Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class IntensiveRepositoryAdapter implements IntensiveRepositoryPort {

    private final JpaIntensiveRepository jpaIntensiveRepository;
    private final IntensivePersistenceMapper mapper;

    @Override
    public Intensive save(Intensive intensive) {
        IntensiveJpaEntity entity = mapper.toJpaEntity(intensive);
        IntensiveJpaEntity saved = jpaIntensiveRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Intensive> findById(Long id) {
        return jpaIntensiveRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Intensive> findByIdForUpdate(Long id) {
        return jpaIntensiveRepository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public Page<Intensive> findWithFilters(IntensiveFilters filters) {
        Specification<IntensiveJpaEntity> spec = IntensiveSpecifications.withFilters(filters);

        Sort sort = "ASC".equalsIgnoreCase(filters.sortDirection())
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageable = PageRequest.of(filters.page(), filters.size(), sort);

        return jpaIntensiveRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public List<Intensive> findAll() {
        return mapper.toDomainList(jpaIntensiveRepository.findAll());
    }

    @Override
    public List<Intensive> findByStatus(IntensiveStatus status) {
        return mapper.toDomainList(jpaIntensiveRepository.findByStatus(status));
    }

    @Override
    public void delete(Long id) {
        jpaIntensiveRepository.deleteById(id);
    }

    @Override
    public long countActiveBySubjectId(Long subjectId) {
        return jpaIntensiveRepository.countBySubjectIdAndStatusIn(
                subjectId, List.of(IntensiveStatus.OPEN, IntensiveStatus.CLOSED)
        );
    }

    @Override
    public long countActiveByTeacherId(Long teacherId) {
        return jpaIntensiveRepository.countByTeacherIdAndStatusIn(
                teacherId, List.of(IntensiveStatus.OPEN, IntensiveStatus.CLOSED)
        );
    }

    @Override
    public long countAllBySubjectId(Long subjectId) {
        return jpaIntensiveRepository.countBySubjectId(subjectId);
    }
}
