package acainfo.back.enrollment.application.usecases;

import acainfo.back.enrollment.application.ports.in.GetEnrollmentUseCase;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.exception.EnrollmentNotFoundException;
import acainfo.back.enrollment.domain.model.EnrollmentDomain;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of GetEnrollmentUseCase
 * Handles retrieving enrollment information
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetEnrollmentUseCaseImpl implements GetEnrollmentUseCase {

    private final EnrollmentRepositoryPort enrollmentRepository;

    @Override
    public EnrollmentDomain getEnrollmentById(Long id) {
        log.debug("Fetching enrollment by ID: {}", id);
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new EnrollmentNotFoundException(id));
    }

    @Override
    public List<EnrollmentDomain> getActiveEnrollmentsByStudent(Long studentId) {
        log.debug("Fetching active enrollments for student: {}", studentId);
        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVO);
    }

    @Override
    public List<EnrollmentDomain> getAllEnrollmentsByStudent(Long studentId) {
        log.debug("Fetching all enrollments for student: {}", studentId);
        return enrollmentRepository.findByStudentId(studentId);
    }

    @Override
    public List<EnrollmentDomain> getEnrollmentsByGroup(Long groupId) {
        log.debug("Fetching all enrollments for group: {}", groupId);
        return enrollmentRepository.findBySubjectGroupId(groupId);
    }

    @Override
    public List<EnrollmentDomain> getActiveEnrollmentsByGroup(Long groupId) {
        log.debug("Fetching active enrollments for group: {}", groupId);
        return enrollmentRepository.findBySubjectGroupIdAndStatus(groupId, EnrollmentStatus.ACTIVO);
    }

    @Override
    public boolean isStudentEnrolled(Long studentId, Long groupId) {
        return enrollmentRepository.existsByStudentIdAndSubjectGroupIdAndStatus(
            studentId, groupId, EnrollmentStatus.ACTIVO
        );
    }
}
