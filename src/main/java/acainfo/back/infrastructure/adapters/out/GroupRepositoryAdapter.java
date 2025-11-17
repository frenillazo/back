package acainfo.back.infrastructure.adapters.out;

import acainfo.back.application.ports.out.GroupRepositoryPort;
import acainfo.back.domain.model.*;
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
public class GroupRepositoryAdapter implements GroupRepositoryPort {

    private final GroupRepository groupRepository;

    @Override
    public Group save(Group group) {
        return groupRepository.save(group);
    }

    @Override
    public Optional<Group> findById(Long id) {
        return groupRepository.findById(id);
    }

    @Override
    public List<Group> findAll() {
        return groupRepository.findAll();
    }

    @Override
    public List<Group> findBySubjectId(Long subjectId) {
        return groupRepository.findBySubjectId(subjectId);
    }

    @Override
    public List<Group> findByTeacherId(Long teacherId) {
        return groupRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<Group> findByStatus(GroupStatus status) {
        return groupRepository.findByStatus(status);
    }

    @Override
    public List<Group> findByType(GroupType type) {
        return groupRepository.findByType(type);
    }

    @Override
    public List<Group> findByPeriod(AcademicPeriod period) {
        return groupRepository.findByPeriod(period);
    }


    @Override
    public List<Group> findGroupsWithAvailablePlaces() {
        return groupRepository.findGroupsWithAvailablePlaces();
    }

    @Override
    public List<Group> findActiveBySubjectId(Long subjectId) {
        return groupRepository.findActiveBySubjectId(subjectId);
    }

    @Override
    public long countBySubjectId(Long subjectId) {
        return groupRepository.countBySubjectId(subjectId);
    }

    @Override
    public long countActiveGroupsBySubjectId(Long subjectId) {
        return groupRepository.countActiveGroupsBySubjectId(subjectId);
    }

    @Override
    public long countByStatus(GroupStatus status) {
        return groupRepository.countByStatus(status);
    }

    @Override
    public boolean hasActiveGroups(Long subjectId) {
        return groupRepository.hasActiveGroups(subjectId);
    }

    @Override
    public void deleteById(Long id) {
        groupRepository.deleteById(id);
    }

    @Override
    public List<Group> findAll(Specification<Group> spec) {
        return groupRepository.findAll(spec);
    }
}
