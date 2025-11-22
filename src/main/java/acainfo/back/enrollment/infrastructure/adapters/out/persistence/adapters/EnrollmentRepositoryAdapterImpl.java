package acainfo.back.enrollment.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.model.AttendanceMode;
import acainfo.back.enrollment.domain.model.EnrollmentDomain;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities.EnrollmentJpaEntity;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.mappers.EnrollmentJpaMapper;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.repositories.EnrollmentJpaRepository;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.repositories.SubjectGroupJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository Adapter Implementation
 * Infrastructure layer - implements domain port using JPA
 *
 * Responsibility: Bridge between domain and JPA infrastructure
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentRepositoryAdapterImpl implements EnrollmentRepositoryPort {

    private final EnrollmentJpaRepository jpaRepository;
    private final EnrollmentJpaMapper mapper;
    private final UserRepository userRepository;
    private final SubjectGroupJpaRepository subjectGroupRepository;

    @Override
    public EnrollmentDomain save(EnrollmentDomain enrollment) {
        if (enrollment == null) {
            return null;
        }

        // Fetch required entities for JPA relationships
        User student = null;
        if (enrollment.getStudentId() != null) {
            student = userRepository.findById(enrollment.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Student not found with ID: " + enrollment.getStudentId()));
        }

        SubjectGroupJpaEntity subjectGroup = null;
        if (enrollment.getSubjectGroupId() != null) {
            subjectGroup = subjectGroupRepository.findById(enrollment.getSubjectGroupId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "SubjectGroup not found with ID: " + enrollment.getSubjectGroupId()));
        }

        // Handle create vs update
        EnrollmentJpaEntity jpaEntity;
        if (enrollment.getId() != null) {
            // Update existing
            jpaEntity = jpaRepository.findById(enrollment.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Enrollment not found with ID: " + enrollment.getId()));
            mapper.updateJpaEntity(jpaEntity, enrollment);
        } else {
            // Create new
            jpaEntity = mapper.toJpaEntity(enrollment, student, subjectGroup);
        }

        EnrollmentJpaEntity saved = jpaRepository.save(jpaEntity);
        log.debug("Saved enrollment with ID: {}", saved.getId());

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<EnrollmentDomain> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<EnrollmentDomain> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDomain> findByStudentId(Long studentId) {
        return jpaRepository.findByStudentId(studentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDomain> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status) {
        return jpaRepository.findByStudentIdAndStatus(studentId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDomain> findBySubjectGroupId(Long groupId) {
        return jpaRepository.findBySubjectGroupId(groupId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDomain> findBySubjectGroupIdAndStatus(Long groupId, EnrollmentStatus status) {
        return jpaRepository.findBySubjectGroupIdAndStatus(groupId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDomain> findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(
            Long groupId,
            EnrollmentStatus status
    ) {
        return jpaRepository.findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(groupId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByStudentIdAndSubjectGroupIdAndStatus(
            Long studentId,
            Long groupId,
            EnrollmentStatus status
    ) {
        return jpaRepository.existsByStudentIdAndSubjectGroupIdAndStatus(studentId, groupId, status);
    }

    @Override
    public long countByStudentIdAndStatus(Long studentId, EnrollmentStatus status) {
        return jpaRepository.countByStudentIdAndStatus(studentId, status);
    }

    @Override
    public long countBySubjectGroupIdAndStatus(Long groupId, EnrollmentStatus status) {
        return jpaRepository.countBySubjectGroupIdAndStatus(groupId, status);
    }

    @Override
    public long countPresentialEnrollments(Long groupId) {
        return jpaRepository.countPresentialEnrollments(groupId);
    }

    @Override
    public long countOnlineEnrollments(Long groupId) {
        return jpaRepository.countOnlineEnrollments(groupId);
    }

    @Override
    public List<EnrollmentDomain> findActiveEnrollmentsByStudentId(Long studentId) {
        return jpaRepository.findActiveEnrollmentsByStudentId(studentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentDomain> findBySubjectGroupIdAndAttendanceMode(Long groupId, AttendanceMode mode) {
        return jpaRepository.findBySubjectGroupIdAndAttendanceMode(groupId, mode).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EnrollmentDomain> findByStudentIdAndSubjectGroupId(Long studentId, Long groupId) {
        return jpaRepository.findByStudentIdAndSubjectGroupId(studentId, groupId)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
        log.debug("Deleted enrollment with ID: {}", id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<EnrollmentDomain> findByStatus(EnrollmentStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
