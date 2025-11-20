package acainfo.back.enrollment.infrastructure.adapters.out;

import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.domain.model.AttendanceMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements EnrollmentRepositoryPort using Spring Data JPA.
 * This adapter bridges the application layer with the infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class EnrollmentRepositoryAdapter implements EnrollmentRepositoryPort {

    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Enrollment save(Enrollment enrollment) {
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        return enrollmentRepository.findById(id);
    }

    @Override
    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    @Override
    public List<Enrollment> findByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status) {
        return enrollmentRepository.findByStudentIdAndStatus(studentId, status);
    }

    @Override
    public List<Enrollment> findBySubjectGroupId(Long groupId) {
        return enrollmentRepository.findBySubjectGroupId(groupId);
    }

    @Override
    public List<Enrollment> findBySubjectGroupIdAndStatus(Long groupId, EnrollmentStatus status) {
        return enrollmentRepository.findBySubjectGroupIdAndStatus(groupId, status);
    }

    @Override
    public List<Enrollment> findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(Long groupId, EnrollmentStatus status) {
        return enrollmentRepository.findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(groupId, status);
    }

    @Override
    public boolean existsByStudentIdAndSubjectGroupIdAndStatus(Long studentId, Long groupId, EnrollmentStatus status) {
        return enrollmentRepository.existsByStudentIdAndSubjectGroupIdAndStatus(studentId, groupId, status);
    }

    @Override
    public long countByStudentIdAndStatus(Long studentId, EnrollmentStatus status) {
        return enrollmentRepository.countByStudentIdAndStatus(studentId, status);
    }

    @Override
    public long countBySubjectGroupIdAndStatus(Long groupId, EnrollmentStatus status) {
        return enrollmentRepository.countBySubjectGroupIdAndStatus(groupId, status);
    }

    @Override
    public long countPresentialEnrollments(Long groupId) {
        return enrollmentRepository.countPresentialEnrollments(groupId);
    }

    @Override
    public long countOnlineEnrollments(Long groupId) {
        return enrollmentRepository.countOnlineEnrollments(groupId);
    }

    @Override
    public List<Enrollment> findActiveEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
    }

    @Override
    public List<Enrollment> findBySubjectGroupIdAndAttendanceMode(Long groupId, AttendanceMode mode) {
        return enrollmentRepository.findBySubjectGroupIdAndAttendanceMode(groupId, mode);
    }

    @Override
    public Optional<Enrollment> findByStudentIdAndSubjectGroupId(Long studentId, Long groupId) {
        return enrollmentRepository.findByStudentIdAndSubjectGroupId(studentId, groupId);
    }

    @Override
    public void deleteById(Long id) {
        enrollmentRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return enrollmentRepository.existsById(id);
    }

    @Override
    public List<Enrollment> findByStatus(EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status);
    }
}
