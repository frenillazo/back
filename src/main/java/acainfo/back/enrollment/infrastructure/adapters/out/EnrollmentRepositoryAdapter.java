package acainfo.back.enrollment.infrastructure.adapters.out;

import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
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
    public List<Enrollment> findBySubjectGroupId(Long subjectGroupId) {
        return enrollmentRepository.findBySubjectGroupId(subjectGroupId);
    }

    @Override
    public List<Enrollment> findByStatus(EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status);
    }

    @Override
    public List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status) {
        return enrollmentRepository.findByStudentIdAndStatus(studentId, status);
    }

    @Override
    public List<Enrollment> findBySubjectGroupIdAndStatus(Long subjectGroupId, EnrollmentStatus status) {
        return enrollmentRepository.findBySubjectGroupIdAndStatus(subjectGroupId, status);
    }

    @Override
    public Optional<Enrollment> findByStudentIdAndSubjectGroupId(Long studentId, Long subjectGroupId) {
        return enrollmentRepository.findByStudentIdAndSubjectGroupId(studentId, subjectGroupId);
    }

    @Override
    public long countByStatus(EnrollmentStatus status) {
        return enrollmentRepository.countByStatus(status);
    }

    @Override
    public long countByStudentId(Long studentId) {
        return enrollmentRepository.countByStudentId(studentId);
    }

    @Override
    public long countActiveByStudentId(Long studentId) {
        return enrollmentRepository.countActiveByStudentId(studentId);
    }

    @Override
    public long countBySubjectGroupId(Long subjectGroupId) {
        return enrollmentRepository.countBySubjectGroupId(subjectGroupId);
    }

    @Override
    public long countActiveBySubjectGroupId(Long subjectGroupId) {
        return enrollmentRepository.countActiveBySubjectGroupId(subjectGroupId);
    }

    @Override
    public boolean existsByStudentIdAndSubjectGroupId(Long studentId, Long subjectGroupId) {
        return enrollmentRepository.existsByStudentIdAndSubjectGroupId(studentId, subjectGroupId);
    }

    @Override
    public boolean existsById(Long id) {
        return enrollmentRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        enrollmentRepository.deleteById(id);
    }

    @Override
    public void delete(Enrollment enrollment) {
        enrollmentRepository.delete(enrollment);
    }
}
