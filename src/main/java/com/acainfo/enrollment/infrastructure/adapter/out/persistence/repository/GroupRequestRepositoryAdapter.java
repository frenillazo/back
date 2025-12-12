package com.acainfo.enrollment.infrastructure.adapter.out.persistence.repository;

import com.acainfo.enrollment.application.dto.GroupRequestFilters;
import com.acainfo.enrollment.application.port.out.GroupRequestRepositoryPort;
import com.acainfo.enrollment.domain.model.GroupRequest;
import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity.GroupRequestJpaEntity;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.specification.GroupRequestSpecifications;
import com.acainfo.enrollment.infrastructure.mapper.GroupRequestPersistenceMapper;
import com.acainfo.group.domain.model.GroupType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing GroupRequestRepositoryPort.
 * Translates domain operations to JPA operations.
 */
@Component
@RequiredArgsConstructor
public class GroupRequestRepositoryAdapter implements GroupRequestRepositoryPort {

    private final JpaGroupRequestRepository jpaGroupRequestRepository;
    private final GroupRequestPersistenceMapper groupRequestPersistenceMapper;

    @Override
    public GroupRequest save(GroupRequest groupRequest) {
        GroupRequestJpaEntity jpaEntity = groupRequestPersistenceMapper.toJpaEntity(groupRequest);
        GroupRequestJpaEntity savedEntity = jpaGroupRequestRepository.save(jpaEntity);
        return groupRequestPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<GroupRequest> findById(Long id) {
        return jpaGroupRequestRepository.findById(id)
                .map(groupRequestPersistenceMapper::toDomain);
    }

    @Override
    public Page<GroupRequest> findWithFilters(GroupRequestFilters filters) {
        Specification<GroupRequestJpaEntity> spec = GroupRequestSpecifications.withFilters(filters);

        Sort sort = filters.sortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageRequest = PageRequest.of(filters.page(), filters.size(), sort);

        return jpaGroupRequestRepository.findAll(spec, pageRequest)
                .map(groupRequestPersistenceMapper::toDomain);
    }

    @Override
    public List<GroupRequest> findBySubjectId(Long subjectId) {
        return groupRequestPersistenceMapper.toDomainList(
                jpaGroupRequestRepository.findBySubjectId(subjectId)
        );
    }

    @Override
    public List<GroupRequest> findByRequesterId(Long requesterId) {
        return groupRequestPersistenceMapper.toDomainList(
                jpaGroupRequestRepository.findByRequesterId(requesterId)
        );
    }

    @Override
    public List<GroupRequest> findByStatus(GroupRequestStatus status) {
        return groupRequestPersistenceMapper.toDomainList(
                jpaGroupRequestRepository.findByStatus(status)
        );
    }

    @Override
    public List<GroupRequest> findPendingBySubjectIdAndType(Long subjectId, GroupType type) {
        return groupRequestPersistenceMapper.toDomainList(
                jpaGroupRequestRepository.findBySubjectIdAndRequestedGroupTypeAndStatus(
                        subjectId, type, GroupRequestStatus.PENDING)
        );
    }

    @Override
    public List<GroupRequest> findExpiredPendingRequests(LocalDateTime dateTime) {
        return groupRequestPersistenceMapper.toDomainList(
                jpaGroupRequestRepository.findByStatusAndExpiresAtBefore(
                        GroupRequestStatus.PENDING, dateTime)
        );
    }

    @Override
    public void delete(Long id) {
        jpaGroupRequestRepository.deleteById(id);
    }
}
