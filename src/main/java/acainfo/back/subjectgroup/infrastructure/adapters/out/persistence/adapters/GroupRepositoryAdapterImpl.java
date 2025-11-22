package acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.mappers.SubjectGroupJpaMapper;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.repositories.SubjectGroupJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Infrastructure Adapter for SubjectGroup persistence
 * Implements GroupRepositoryPort
 * Uses SubjectGroupJpaMapper to convert between Domain and JPA entities
 * Delegates to SubjectGroupJpaRepository for database operations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupRepositoryAdapterImpl implements GroupRepositoryPort {

    private final SubjectGroupJpaRepository jpaRepository;
    private final SubjectGroupJpaMapper mapper;

    @Override
    public SubjectGroupDomain save(SubjectGroupDomain subjectGroup) {
        log.debug("Saving subjectGroup: {}", subjectGroup);

        SubjectGroupJpaEntity jpaEntity = mapper.toJpaEntity(subjectGroup);
        SubjectGroupJpaEntity saved = jpaRepository.save(jpaEntity);

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SubjectGroupDomain> findById(Long id) {
        log.debug("Finding subjectGroup by ID: {}", id);

        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<SubjectGroupDomain> findAll() {
        log.debug("Finding all subjectGroups");

        return mapper.toDomains(jpaRepository.findAll());
    }

    @Override
    public List<SubjectGroupDomain> findBySubjectId(Long subjectId) {
        log.debug("Finding subjectGroups by subject ID: {}", subjectId);

        return mapper.toDomains(jpaRepository.findBySubjectId(subjectId));
    }

    @Override
    public List<SubjectGroupDomain> findByTeacherId(Long teacherId) {
        log.debug("Finding subjectGroups by teacher ID: {}", teacherId);

        return mapper.toDomains(jpaRepository.findByTeacherId(teacherId));
    }

    @Override
    public List<SubjectGroupDomain> findByStatus(GroupStatus status) {
        log.debug("Finding subjectGroups by status: {}", status);

        return mapper.toDomains(jpaRepository.findByStatus(status));
    }

    @Override
    public List<SubjectGroupDomain> findByType(GroupType type) {
        log.debug("Finding subjectGroups by type: {}", type);

        return mapper.toDomains(jpaRepository.findByType(type));
    }

    @Override
    public List<SubjectGroupDomain> findByPeriod(AcademicPeriod period) {
        log.debug("Finding subjectGroups by period: {}", period);

        return mapper.toDomains(jpaRepository.findByPeriod(period));
    }

    @Override
    public List<SubjectGroupDomain> findGroupsWithAvailablePlaces() {
        log.debug("Finding subjectGroups with available places");

        return mapper.toDomains(jpaRepository.findGroupsWithAvailablePlaces());
    }

    @Override
    public List<SubjectGroupDomain> findActiveBySubjectId(Long subjectId) {
        log.debug("Finding active subjectGroups by subject ID: {}", subjectId);

        return mapper.toDomains(jpaRepository.findActiveBySubjectId(subjectId));
    }

    @Override
    public long countBySubjectId(Long subjectId) {
        log.debug("Counting subjectGroups by subject ID: {}", subjectId);

        return jpaRepository.countBySubjectId(subjectId);
    }

    @Override
    public long countActiveGroupsBySubjectId(Long subjectId) {
        log.debug("Counting active subjectGroups by subject ID: {}", subjectId);

        return jpaRepository.countActiveGroupsBySubjectId(subjectId);
    }

    @Override
    public long countByStatus(GroupStatus status) {
        log.debug("Counting subjectGroups by status: {}", status);

        return jpaRepository.countByStatus(status);
    }

    @Override
    public boolean hasActiveGroups(Long subjectId) {
        log.debug("Checking if subject has active groups: {}", subjectId);

        return jpaRepository.hasActiveGroups(subjectId);
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting subjectGroup by ID: {}", id);

        jpaRepository.deleteById(id);
    }

    @Override
    public Boolean existsById(Long id) {
        log.debug("Checking if subjectGroup exists by ID: {}", id);

        return jpaRepository.existsById(id);
    }
}
