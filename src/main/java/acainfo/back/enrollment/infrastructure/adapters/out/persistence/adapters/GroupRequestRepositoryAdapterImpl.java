package acainfo.back.enrollment.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.enrollment.application.ports.out.GroupRequestRepositoryPort;
import acainfo.back.enrollment.domain.model.GroupRequestDomain;
import acainfo.back.enrollment.domain.model.GroupRequestStatus;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities.GroupRequestJpaEntity;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.mappers.GroupRequestJpaMapper;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.repositories.GroupRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter that implements GroupRequestRepositoryPort using Spring Data JPA.
 * This adapter bridges the application layer with the infrastructure layer.
 * Converts between GroupRequestDomain and GroupRequestJpaEntity.
 */
@Component
@RequiredArgsConstructor
public class GroupRequestRepositoryAdapterImpl implements GroupRequestRepositoryPort {

    private final GroupRequestJpaRepository groupRequestJpaRepository;
    private final GroupRequestJpaMapper groupRequestJpaMapper;

    @Override
    public GroupRequestDomain save(GroupRequestDomain groupRequest) {
        GroupRequestJpaEntity entity;

        // If it's an existing entity, update it
        if (groupRequest.getId() != null) {
            entity = groupRequestJpaRepository.findById(groupRequest.getId())
                    .orElseThrow(() -> new IllegalArgumentException("GroupRequest not found with id: " + groupRequest.getId()));
            groupRequestJpaMapper.updateEntity(entity, groupRequest);
        } else {
            // New entity
            entity = groupRequestJpaMapper.toEntity(groupRequest);
        }

        GroupRequestJpaEntity saved = groupRequestJpaRepository.save(entity);
        return groupRequestJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<GroupRequestDomain> findById(Long id) {
        return groupRequestJpaRepository.findById(id)
                .map(groupRequestJpaMapper::toDomain);
    }

    @Override
    public List<GroupRequestDomain> findAll() {
        return groupRequestJpaRepository.findAll().stream()
                .map(groupRequestJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupRequestDomain> findBySubjectId(Long subjectId) {
        return groupRequestJpaRepository.findBySubjectId(subjectId).stream()
                .map(groupRequestJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupRequestDomain> findByRequesterId(Long requesterId) {
        return groupRequestJpaRepository.findByRequesterId(requesterId).stream()
                .map(groupRequestJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupRequestDomain> findByStatus(GroupRequestStatus status) {
        return groupRequestJpaRepository.findByStatus(status).stream()
                .map(groupRequestJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupRequestDomain> findPendingBySubjectId(Long subjectId) {
        return groupRequestJpaRepository.findPendingBySubjectId(subjectId).stream()
                .map(groupRequestJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<GroupRequestDomain> findPendingRequestBySubjectId(Long subjectId) {
        return groupRequestJpaRepository.findPendingRequestBySubjectId(subjectId)
                .map(groupRequestJpaMapper::toDomain);
    }

    @Override
    public boolean existsPendingRequestBySubjectId(Long subjectId) {
        return groupRequestJpaRepository.existsPendingRequestBySubjectId(subjectId);
    }

    @Override
    public List<GroupRequestDomain> findRequestsSupportedByStudent(Long studentId) {
        return groupRequestJpaRepository.findRequestsSupportedByStudent(studentId).stream()
                .map(groupRequestJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isStudentSupporter(Long requestId, Long studentId) {
        return groupRequestJpaRepository.isStudentSupporter(requestId, studentId);
    }

    @Override
    public int countPendingByStudentId(Long studentId) {
        return groupRequestJpaRepository.countPendingByStudentId(studentId);
    }

    @Override
    public void deleteById(Long id) {
        groupRequestJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return groupRequestJpaRepository.existsById(id);
    }
}
