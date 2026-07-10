package com.acainfo.enrollment.infrastructure.adapter.out.persistence.repository;

import com.acainfo.enrollment.application.dto.EnrollmentFilters;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity.EnrollmentJpaEntity;
import com.acainfo.enrollment.infrastructure.adapter.out.persistence.specification.EnrollmentSpecifications;
import com.acainfo.enrollment.infrastructure.mapper.EnrollmentPersistenceMapper;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
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
    private final GetUserProfileUseCase getUserProfileUseCase;

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
        Sort sort = filters.sortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(filters.sortBy()).ascending()
                : Sort.by(filters.sortBy()).descending();

        PageRequest pageRequest = PageRequest.of(filters.page(), filters.size(), sort);

        // If filtering by studentEmail, first find matching student IDs
        List<Long> studentIdsFromEmail = null;
        if (filters.studentEmail() != null && !filters.studentEmail().isBlank()) {
            studentIdsFromEmail = getUserProfileUseCase.findIdsByEmailContaining(filters.studentEmail());
            if (studentIdsFromEmail.isEmpty()) {
                // No students match the email filter, return empty page
                return new PageImpl<>(Collections.emptyList(), pageRequest, 0);
            }
        }

        Specification<EnrollmentJpaEntity> spec = EnrollmentSpecifications.withFilters(filters);

        // Add studentIds filter if searching by email
        if (studentIdsFromEmail != null) {
            spec = spec.and(EnrollmentSpecifications.hasStudentIdIn(studentIdsFromEmail));
        }

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
    public List<Enrollment> findByCourseId(Long courseId) {
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByCourseId(courseId)
        );
    }

    @Override
    public List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status) {
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByStudentIdAndStatus(studentId, status)
        );
    }

    @Override
    public List<Enrollment> findByStudentIdAndStatusIn(Long studentId, List<EnrollmentStatus> statuses) {
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByStudentIdAndStatusIn(studentId, statuses)
        );
    }

    @Override
    public List<Enrollment> findByCourseIdAndStatus(Long courseId, EnrollmentStatus status) {
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByCourseIdAndStatus(courseId, status)
        );
    }

    @Override
    public Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId) {
        return jpaEnrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .map(enrollmentPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsActiveOrWaitingEnrollment(Long studentId, Long courseId) {
        return jpaEnrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
                studentId,
                courseId,
                List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.WAITING_LIST)
        );
    }

    @Override
    public boolean existsActiveOrWaitingOrPendingEnrollment(Long studentId, Long courseId) {
        return jpaEnrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
                studentId,
                courseId,
                List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.WAITING_LIST, EnrollmentStatus.PENDING_APPROVAL)
        );
    }

    @Override
    public List<Enrollment> findPendingApprovalByCourseIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByCourseIdInAndStatus(courseIds, EnrollmentStatus.PENDING_APPROVAL)
        );
    }

    @Override
    public List<Enrollment> findExpiredPendingEnrollments(int hoursOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursOld);
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByStatusAndEnrolledAtBefore(
                        EnrollmentStatus.PENDING_APPROVAL, cutoffTime)
        );
    }

    @Override
    public long countActiveByCourseId(Long courseId) {
        return jpaEnrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
    }

    @Override
    public List<Enrollment> findWaitingListByCourseId(Long courseId) {
        return enrollmentPersistenceMapper.toDomainList(
                jpaEnrollmentRepository.findByCourseIdAndStatusOrderByWaitingListPositionAsc(
                        courseId, EnrollmentStatus.WAITING_LIST)
        );
    }

    @Override
    public int getNextWaitingListPosition(Long courseId) {
        return jpaEnrollmentRepository.findNextWaitingListPosition(courseId);
    }

    @Override
    @Transactional
    public void decrementWaitingListPositionsAfter(Long courseId, int position) {
        jpaEnrollmentRepository.decrementWaitingListPositionsAfter(courseId, position);
    }

    @Override
    public void delete(Long id) {
        jpaEnrollmentRepository.deleteById(id);
    }
}
