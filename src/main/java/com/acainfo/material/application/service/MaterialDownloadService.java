package com.acainfo.material.application.service;

import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.material.application.dto.MaterialDownload;
import com.acainfo.material.application.port.in.DownloadMaterialUseCase;
import com.acainfo.material.application.port.in.PreviewMaterialUseCase;
import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.MaterialAccessDeniedException;
import com.acainfo.material.domain.exception.MaterialNotFoundException;
import com.acainfo.material.domain.model.Material;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
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
 *   <li>Students need an ACTIVE enrollment in a course of the material's subject</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialDownloadService implements DownloadMaterialUseCase, PreviewMaterialUseCase {

    private final MaterialRepositoryPort materialRepository;
    private final FileStoragePort fileStorage;
    private final UserRepositoryPort userRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final CourseRepositoryPort courseRepository;

    @Override
    public MaterialDownload download(Long materialId, Long userId) {
        log.debug("Download request for material {} by user {}", materialId, userId);

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // Download requires the full check, including downloadDisabled
        if (!canUserAccess(material, userId, /* checkDownloadDisabled */ true)) {
            throw new MaterialAccessDeniedException(materialId, userId);
        }

        InputStream content = fileStorage.retrieve(material.getStoragePath());
        log.info("Material downloaded: id={}, user={}", materialId, userId);

        return new MaterialDownload(
                material.getOriginalFilename(),
                material.getMimeType(),
                material.getFileSize(),
                content
        );
    }

    @Override
    public MaterialDownload preview(Long materialId, Long userId) {
        log.debug("Preview request for material {} by user {}", materialId, userId);

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // Preview ignores downloadDisabled — a hidden-from-download material can still
        // be opened in the in-app viewer if it remains visible.
        if (!canUserAccess(material, userId, /* checkDownloadDisabled */ false)) {
            throw new MaterialAccessDeniedException(materialId, userId);
        }

        InputStream content = fileStorage.retrieve(material.getStoragePath());
        log.info("Material previewed: id={}, user={}", materialId, userId);

        return new MaterialDownload(
                material.getOriginalFilename(),
                material.getMimeType(),
                material.getFileSize(),
                content
        );
    }

    /**
     * Shared access check for download and preview.
     *
     * @param checkDownloadDisabled when true, materials with {@code downloadDisabled=true}
     *                              are denied to non-admin/teacher users; when false the
     *                              flag is ignored (used by the in-app viewer).
     */
    private boolean canUserAccess(Material material, Long userId, boolean checkDownloadDisabled) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // Admins and teachers can always access (download & preview)
        if (user.isAdmin() || user.isTeacher()) {
            return true;
        }

        // Hidden materials are never accessible to non-admin users
        if (!material.isVisible()) {
            return false;
        }

        // Download endpoint additionally enforces the downloadDisabled flag
        if (checkDownloadDisabled && material.isDownloadDisabled()) {
            return false;
        }

        // Students need an ACTIVE enrollment in a course of the material's subject
        if (user.isStudent()) {
            return hasActiveEnrollmentInSubject(userId, material.getSubjectId());
        }

        return false;
    }

    /**
     * Check if student has an ACTIVE enrollment in any course of the subject.
     */
    private boolean hasActiveEnrollmentInSubject(Long studentId, Long subjectId) {
        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE)
                .stream()
                .anyMatch(enrollment -> courseRepository.findById(enrollment.getCourseId())
                        .map(course -> subjectId.equals(course.getSubjectId()))
                        .orElse(false));
    }
}
