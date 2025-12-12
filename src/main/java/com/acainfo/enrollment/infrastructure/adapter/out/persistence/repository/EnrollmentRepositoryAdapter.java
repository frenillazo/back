package com.acainfo.enrollment.infrastructure.adapter.out.persistence.repository;

import com.acainfo.enrollment.application.dto.EnrollmentFilters;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity.EnrollmentJpaEntity;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.specification.EnrollmentSpecifications;
import com.acainfo.enrollment.infrastructure.mapper.EnrollmentPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing EnrollmentRepositoryPort.
 * Translates domain operations to JPA operations.
 */
@Component
@RequiredArgsConstructor
public class EnrollmentRepositoryAdapter implements EnrollmentRepositoryPort {

    private final JpaEnrollmentRepository jpaEnrollmentRepository;
    private final EnrollmentPersistenceMapper enrollmentPersistenceMapper;

    @Override
    public Enrollment save(Enrollment enrollment) {
        EnrollmentJpaEntity jpaEntity = enrollmentPersistenceMapper.toJpaEntity(enrollment);
        EnrollmentJpaEntity savedEntity = jpaEnrollmentRepository.save(jpaEntity);
        return enrollmentPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        return jpaEnrollmentRepository.findById(id)
                .map(enrollmentPersistenceMapper::toDomain);
    }

    @Override
    public Page<Enrollment> findWithFilters(EnrollmentFilters filters) {
        Specification<EnrollmentJpaEntity> spec = EnrollmentSpecifications.withFilters(filters);

        Sort sort = filters.sortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageRequest = PageRequest.of(filters.page(), filters.size(), sort);

        return jpaEnrollmentRepository.findAll(spec, pageRequest)
                .map(enrollmentPersistenceMapper::toDomain);
    }

    @Override
    public List<Enrollment> findByStudentId(Long studentId) {
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByStudentId(studentId)
        );
    }

    @Override
    public List<Enrollment> findByGroupId(Long groupId) {
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByGroupId(groupId)
        );
    }

    @Override
    public List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status) {
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByStudentIdAndStatus(studentId, status)
        );
    }

    @Override
    public List<Enrollment> findByGroupIdAndStatus(Long groupId, EnrollmentStatus status) {
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByGroupIdAndStatus(groupId, status)
        );
    }

    @Override
    public Optional<Enrollment> findByStudentIdAndGroupId(Long studentId, Long groupId) {
        return jpaEnrollmentRepository.findByStudentIdAndGroupId(studentId, groupId)
                .map(enrollmentPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsActiveOrWaitingEnrollment(Long studentId, Long groupId) {
        return jpaEnrollmentRepository.existsByStudentIdAndGroupIdAndStatusIn(
                studentId,
                groupId,
                List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.WAITING_LIST)
        );
    }

    @Override
    public long countActiveByGroupId(Long groupId) {
        return jpaEnrollmentRepository.countByGroupIdAndStatus(groupId, EnrollmentStatus.ACTIVE);
    }

    @Override
    public List<Enrollment> findWaitingListByGroupId(Long groupId) {
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByGroupIdAndStatusOrderByWaitingListPositionAsc(
                        groupId, EnrollmentStatus.WAITING_LIST)
        );
    }

    @Override
    public int getNextWaitingListPosition(Long groupId) {
        return jpaEnrollmentRepository.findNextWaitingListPosition(groupId);
    }

    @Override
    @Transactional
    public void decrementWaitingListPositionsAfter(Long groupId, int position) {
        jpaEnrollmentRepository.decrementWaitingListPositionsAfter(groupId, position);
    }

    @Override
    public void delete(Long id) {
        jpaEnrollmentRepository.deleteById(id);
    }
}
