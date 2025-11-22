package acainfo.back.subjectgroup.infrastructure.adapters.out;

import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.mappers.SubjectGroupJpaMapper;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.repositories.SubjectGroupJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements GroupRepositoryPort using Spring Data JPA.
 * This adapter bridges the application layer with the infrastructure layer.
 * Converts between SubjectGroupDomain (application layer) and SubjectGroupJpaEntity (infrastructure layer).
 */
@Component
@RequiredArgsConstructor
public class SubjectGroupRepositoryAdapter implements GroupRepositoryPort {

    private final SubjectGroupJpaRepository subjectGroupRepository;
    private final SubjectGroupJpaMapper mapper;

    @Override
    public SubjectGroupDomain save(SubjectGroupDomain subjectGroup) {
        SubjectGroupJpaEntity jpaEntity = mapper.toJpaEntity(subjectGroup);
        SubjectGroupJpaEntity savedEntity = subjectGroupRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SubjectGroupDomain> findById(Long id) {
        return subjectGroupRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<SubjectGroupDomain> findAll() {
        return mapper.toDomains(subjectGroupRepository.findAll());
    }

    @Override
    public List<SubjectGroupDomain> findBySubjectId(Long subjectId) {
        return mapper.toDomains(subjectGroupRepository.findBySubjectId(subjectId));
    }

    @Override
    public List<SubjectGroupDomain> findByTeacherId(Long teacherId) {
        return mapper.toDomains(subjectGroupRepository.findByTeacherId(teacherId));
    }

    @Override
    public List<SubjectGroupDomain> findByStatus(GroupStatus status) {
        return mapper.toDomains(subjectGroupRepository.findByStatus(status));
    }

    @Override
    public List<SubjectGroupDomain> findByType(GroupType type) {
        return mapper.toDomains(subjectGroupRepository.findByType(type));
    }

    @Override
    public List<SubjectGroupDomain> findByPeriod(AcademicPeriod period) {
        return mapper.toDomains(subjectGroupRepository.findByPeriod(period));
    }


    @Override
    public List<SubjectGroupDomain> findGroupsWithAvailablePlaces() {
        return mapper.toDomains(subjectGroupRepository.findGroupsWithAvailablePlaces());
    }

    @Override
    public List<SubjectGroupDomain> findActiveBySubjectId(Long subjectId) {
        return mapper.toDomains(subjectGroupRepository.findActiveBySubjectId(subjectId));
    }

    @Override
    public long countBySubjectId(Long subjectId) {
        return subjectGroupRepository.countBySubjectId(subjectId);
    }

    @Override
    public long countActiveGroupsBySubjectId(Long subjectId) {
        return subjectGroupRepository.countActiveGroupsBySubjectId(subjectId);
    }

    @Override
    public Boolean existsById(Long id) {
        return subjectGroupRepository.existsById(id);
    }

    @Override
    public long countByStatus(GroupStatus status) {
        return subjectGroupRepository.countByStatus(status);
    }

    @Override
    public boolean hasActiveGroups(Long subjectId) {
        return subjectGroupRepository.hasActiveGroups(subjectId);
    }

    @Override
    public void deleteById(Long id) {
        subjectGroupRepository.deleteById(id);
    }

    @Override
    public List<SubjectGroupDomain> findAll(Specification<SubjectGroupJpaEntity> spec) {
        return mapper.toDomains(subjectGroupRepository.findAll(spec));
    }
}
