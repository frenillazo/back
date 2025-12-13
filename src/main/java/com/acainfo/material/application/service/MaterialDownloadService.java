package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.MaterialDownload;
import com.acainfo.material.application.port.in.DownloadMaterialUseCase;
import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.MaterialAccessDeniedException;
import com.acainfo.material.domain.exception.MaterialNotFoundException;
import com.acainfo.material.domain.model.Material;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.payment.application.port.in.CheckPaymentStatusUseCase;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

/**
 * Service for material download operations with access control.
 * Implements DownloadMaterialUseCase.
 *
 * <p>Access rules:</p>
 * <ul>
 *   <li>Admins and teachers can always download</li>
 *   <li>Students need active enrollment in a group of the subject</li>
 *   <li>Students need payments up to date (no overdue payments)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialDownloadService implements DownloadMaterialUseCase {

    private final MaterialRepositoryPort materialRepository;
    private final FileStoragePort fileStorage;
    private final UserRepositoryPort userRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final CheckPaymentStatusUseCase checkPaymentStatus;

    @Override
    public MaterialDownload download(Long materialId, Long userId) {
        log.debug("Download request for material {} by user {}", materialId, userId);

        // 1. Find material
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // 2. Check access
        if (!canUserDownload(material, userId)) {
            throw new MaterialAccessDeniedException(materialId, userId);
        }

        // 3. Retrieve file content
        InputStream content = fileStorage.retrieve(material.getStoragePath());

        log.info("Material downloaded: id={}, user={}", materialId, userId);

        return new MaterialDownload(
                material.getOriginalFilename(),
                material.getMimeType(),
                material.getFileSize(),
                content
        );
    }

    /**
     * Check if user can download the material.
     */
    private boolean canUserDownload(Material material, Long userId) {
        // Get user
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // Admins and teachers can always download
        if (user.isAdmin() || user.isTeacher()) {
            return true;
        }

        // Students need active enrollment in a group of the subject
        if (user.isStudent()) {
            return hasActiveEnrollmentInSubject(userId, material.getSubjectId())
                    && hasPaymentsUpToDate(userId);
        }

        return false;
    }

    /**
     * Check if student has active enrollment in any group of the subject.
     */
    private boolean hasActiveEnrollmentInSubject(Long studentId, Long subjectId) {
        // Get all active enrollments for student
        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE)
                .stream()
                .anyMatch(enrollment -> isEnrollmentForSubject(enrollment.getGroupId(), subjectId));
    }

    /**
     * Check if a group belongs to a subject.
     * This is a simplified check - in a real implementation we'd query the group.
     */
    private boolean isEnrollmentForSubject(Long groupId, Long subjectId) {
        // TODO: This should query GroupRepositoryPort to check if group belongs to subject
        // For now, we assume enrollment gives access to all materials
        // This will be refined when we have proper cross-module queries
        return true;
    }

    /**
     * Check if student has payments up to date (no overdue).
     */
    private boolean hasPaymentsUpToDate(Long studentId) {
        return checkPaymentStatus.canAccessResources(studentId);
    }
}
