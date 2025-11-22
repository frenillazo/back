package acainfo.back.material.application.usecases;

import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.material.application.ports.in.DownloadMaterialUseCase;
import acainfo.back.material.application.ports.out.MaterialRepositoryPort;
import acainfo.back.material.application.services.FileStorageService;
import acainfo.back.material.domain.exception.MaterialNotFoundException;
import acainfo.back.material.domain.exception.UnauthorizedMaterialAccessException;
import acainfo.back.material.domain.model.MaterialDomain;
import acainfo.back.payment.application.services.PaymentService;
import acainfo.back.payment.domain.exception.OverduePaymentException;
import acainfo.back.shared.domain.exception.UserNotFoundException;
import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of DownloadMaterialUseCase
 * Handles downloading educational material files with access control
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DownloadMaterialUseCaseImpl implements DownloadMaterialUseCase {

    private final MaterialRepositoryPort materialRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final PaymentService paymentService;

    @Override
    public Resource downloadMaterial(Long materialId, Long userId) {
        log.debug("User {} requesting download of material {}", userId, materialId);

        // 1. Validate material exists and is active
        MaterialDomain material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        if (!material.getIsActive()) {
            log.warn("User {} attempted to access inactive material {}", userId, materialId);
            throw new MaterialNotFoundException(materialId);
        }

        // 2. Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 3. Check access permissions
        validateMaterialAccess(material, user);

        // 4. Load and return file
        Resource resource = fileStorageService.loadFileAsResource(material.getFilePath());

        log.info("User {} successfully downloaded material {}. File: {}",
                userId, materialId, material.getFileName());

        return resource;
    }

    @Override
    public String getFileName(Long materialId) {
        MaterialDomain material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));
        return material.getFileName();
    }

    /**
     * Validates that a user has access to a material.
     * Access rules:
     * 1. Teachers and admins have full access
     * 2. Students need active enrollment in the subject group
     * 3. If material requires payment, student must have no overdue payments (>5 days)
     */
    private void validateMaterialAccess(MaterialDomain material, User user) {
        // Admins and teachers have full access
        if (user.isAdmin() || user.isTeacher()) {
            log.debug("User {} has admin/teacher access to material {}", user.getId(), material.getId());
            return;
        }

        // Students need active enrollment
        Optional<Enrollment> enrollments = enrollmentRepository
                .findByStudentIdAndSubjectGroupId(user.getId(), material.getSubjectGroupId());

        boolean hasActiveEnrollment = enrollments.stream()
                .anyMatch(e -> e.getStatus() == EnrollmentStatus.ACTIVO);

        if (!hasActiveEnrollment) {
            log.warn("User {} attempted to access material {} without active enrollment",
                    user.getId(), material.getId());
            throw new UnauthorizedMaterialAccessException(
                    "You must be enrolled in the subject group to access this material");
        }

        // If material requires payment, validate payment status
        if (material.getRequiresPayment()) {
            log.debug("Material {} requires payment validation for user {}",
                    material.getId(), user.getId());

            try {
                paymentService.validateNoOverduePayments(user.getId());
            } catch (OverduePaymentException e) {
                log.warn("User {} attempted to access material {} with overdue payments",
                        user.getId(), material.getId());
                throw new UnauthorizedMaterialAccessException(
                        "You have overdue payments. Please pay pending fees to access this material.");
            }
        }

        log.debug("User {} has valid access to material {}", user.getId(), material.getId());
    }
}
