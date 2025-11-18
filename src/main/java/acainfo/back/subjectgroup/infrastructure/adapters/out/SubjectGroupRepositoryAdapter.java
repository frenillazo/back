package acainfo.back.subjectgroup.infrastructure.adapters.out;

import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements GroupRepositoryPort using Spring Data JPA.
 * This adapter bridges the application layer with the infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class SubjectGroupRepositoryAdapter implements GroupRepositoryPort {

    private final SubjectGroupRepository subjectGroupRepository;

    @Override
    public SubjectGroup save(SubjectGroup subjectGroup) {
        return subjectGroupRepository.save(subjectGroup);
    }

    @Override
    public Optional<SubjectGroup> findById(Long id) {
        return subjectGroupRepository.findById(id);
    }

    @Override
    public List<SubjectGroup> findAll() {
        return subjectGroupRepository.findAll();
    }

    @Override
    public List<SubjectGroup> findBySubjectId(Long subjectId) {
        return subjectGroupRepository.findBySubjectId(subjectId);
    }

    @Override
    public List<SubjectGroup> findByTeacherId(Long teacherId) {
        return subjectGroupRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<SubjectGroup> findByStatus(GroupStatus status) {
        return subjectGroupRepository.findByStatus(status);
    }

    @Override
    public List<SubjectGroup> findByType(GroupType type) {
        return subjectGroupRepository.findByType(type);
    }

    @Override
    public List<SubjectGroup> findByPeriod(AcademicPeriod period) {
        return subjectGroupRepository.findByPeriod(period);
    }


    @Override
    public List<SubjectGroup> findGroupsWithAvailablePlaces() {
        return subjectGroupRepository.findGroupsWithAvailablePlaces();
    }

    @Override
    public List<SubjectGroup> findActiveBySubjectId(Long subjectId) {
        return subjectGroupRepository.findActiveBySubjectId(subjectId);
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
    public List<SubjectGroup> findAll(Specification<SubjectGroup> spec) {
        return subjectGroupRepository.findAll(spec);
    }
}
