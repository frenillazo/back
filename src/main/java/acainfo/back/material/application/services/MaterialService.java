package acainfo.back.material.application.services;

import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.material.application.ports.in.DownloadMaterialUseCase;
import acainfo.back.material.application.ports.in.ManageMaterialUseCase;
import acainfo.back.material.application.ports.in.UploadMaterialUseCase;
import acainfo.back.material.application.ports.out.MaterialRepositoryPort;
import acainfo.back.material.domain.exception.InvalidFileTypeException;
import acainfo.back.material.domain.exception.MaterialNotFoundException;
import acainfo.back.material.domain.exception.UnauthorizedMaterialAccessException;
import acainfo.back.material.domain.model.Material;
import acainfo.back.material.domain.model.MaterialType;
import acainfo.back.payment.application.services.PaymentService;
import acainfo.back.payment.domain.exception.OverduePaymentException;
import acainfo.back.user.domain.model.User;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.user.infrastructure.adapters.out.UserRepository;
import acainfo.back.user.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing educational materials.
 *
 * Business Rules:
 * 1. Only teachers can upload materials
 * 2. Students need active enrollment to access materials
 * 3. Materials with requiresPayment=true require student to have no overdue payments (>5 days)
 * 4. Supported file types: PDF, Java, C++, Header files
 * 5. Files are stored locally organized by subject group
 * 6. Soft delete is used (isActive flag)
 * 7. Only uploader or admin can update/delete materials
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MaterialService implements UploadMaterialUseCase, DownloadMaterialUseCase, ManageMaterialUseCase {

    private final MaterialRepositoryPort materialRepository;
    private final GroupRepositoryPort groupRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final PaymentService paymentService;

    // ==================== UPLOAD MATERIAL ====================

    @Override
    @Transactional
    public Material uploadMaterial(
            MultipartFile file,
            Long subjectGroupId,
            Long uploaderId,
            String description,
            String topic,
            Boolean requiresPayment
    ) {
        log.info("Uploading material to group {} by user {}. File: {}, Size: {} bytes",
            subjectGroupId, uploaderId, file.getOriginalFilename(), file.getSize());

        // 1. Validate file is not empty
        if (file.isEmpty()) {
            throw new InvalidFileTypeException("Cannot upload empty file");
        }

        // 2. Validate and determine file type
        String fileName = file.getOriginalFilename();
        MaterialType type = MaterialType.fromFileName(fileName);

        // 3. Validate subject group exists
        SubjectGroup group = groupRepository.findById(subjectGroupId)
                .orElseThrow(() -> new GroupNotFoundException(subjectGroupId));

        // 4. Validate uploader is a teacher
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new UserNotFoundException(uploaderId));

        if (!uploader.isTeacher() && !uploader.isAdmin()) {
            log.warn("User {} attempted to upload material but is not a teacher", uploaderId);
            throw new UnauthorizedMaterialAccessException("Only teachers can upload materials");
        }

        // 5. Store file in file system
        String filePath = fileStorageService.storeFile(file, subjectGroupId);
        long fileSize = file.getSize();

        // 6. Create material entity
        Material material = Material.builder()
                .subjectGroup(group)
                .fileName(fileName)
                .filePath(filePath)
                .type(type)
                .fileSize(fileSize)
                .description(description)
                .topic(topic)
                .uploadedBy(uploader)
                .requiresPayment(requiresPayment != null ? requiresPayment : true)
                .isActive(true)
                .build();

        // 7. Save to database
        Material savedMaterial = materialRepository.save(material);

        log.info("Material uploaded successfully. ID: {}, File: {}, Type: {}, Size: {}",
            savedMaterial.getId(), fileName, type, savedMaterial.getFormattedFileSize());

        return savedMaterial;
    }

    // ==================== DOWNLOAD MATERIAL ====================

    @Override
    @Transactional(readOnly = true)
    public Resource downloadMaterial(Long materialId, Long userId) {
        log.debug("User {} requesting download of material {}", userId, materialId);

        // 1. Validate material exists and is active
        Material material = materialRepository.findById(materialId)
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
    @Transactional(readOnly = true)
    public String getFileName(Long materialId) {
        Material material = materialRepository.findById(materialId)
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
    private void validateMaterialAccess(Material material, User user) {
        // Admins and teachers have full access
        if (user.isAdmin() || user.isTeacher()) {
            log.debug("User {} has admin/teacher access to material {}", user.getId(), material.getId());
            return;
        }

        // Students need active enrollment
        Optional<Enrollment> enrollments = enrollmentRepository
                .findByStudentIdAndSubjectGroupId(user.getId(), material.getSubjectGroup().getId());

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

    // ==================== MANAGE MATERIAL ====================

    @Override
    @Transactional(readOnly = true)
    public Material getMaterialById(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> getMaterialsBySubjectGroup(Long subjectGroupId) {
        log.debug("Fetching active materials for group {}", subjectGroupId);
        return materialRepository.findBySubjectGroupIdAndIsActiveTrue(subjectGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> getMaterialsBySubjectGroupAndType(Long subjectGroupId, MaterialType type) {
        log.debug("Fetching materials for group {} and type {}", subjectGroupId, type);
        return materialRepository.findBySubjectGroupIdAndTypeAndIsActiveTrue(subjectGroupId, type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> getMaterialsBySubjectGroupAndTopic(Long subjectGroupId, String topic) {
        log.debug("Fetching materials for group {} and topic '{}'", subjectGroupId, topic);
        return materialRepository.findBySubjectGroupIdAndTopicAndIsActiveTrue(subjectGroupId, topic);
    }

    @Override
    @Transactional
    public Material updateMaterial(
            Long materialId,
            Long userId,
            String description,
            String topic,
            Boolean requiresPayment
    ) {
        log.info("User {} updating material {}", userId, materialId);

        // 1. Get material
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // 2. Validate user is authorized (uploader or admin)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.isAdmin() && !material.getUploadedBy().getId().equals(userId)) {
            log.warn("User {} attempted to update material {} without authorization", userId, materialId);
            throw new UnauthorizedMaterialAccessException("Only the uploader or admin can update this material");
        }

        // 3. Update fields
        if (description != null) {
            material.setDescription(description);
        }
        if (topic != null) {
            material.setTopic(topic);
        }
        if (requiresPayment != null) {
            material.setRequiresPayment(requiresPayment);
        }

        // 4. Save
        Material updated = materialRepository.save(material);
        log.info("Material {} updated successfully", materialId);

        return updated;
    }

    @Override
    @Transactional
    public void deactivateMaterial(Long materialId, Long userId) {
        log.info("User {} deactivating material {}", userId, materialId);

        // 1. Get material
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // 2. Validate user is authorized
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.isAdmin() && !material.getUploadedBy().getId().equals(userId)) {
            log.warn("User {} attempted to deactivate material {} without authorization", userId, materialId);
            throw new UnauthorizedMaterialAccessException("Only the uploader or admin can deactivate this material");
        }

        // 3. Deactivate (soft delete)
        material.deactivate();
        materialRepository.save(material);

        log.info("Material {} deactivated successfully", materialId);
    }

    @Override
    @Transactional
    public void deleteMaterialPermanently(Long materialId, Long adminId) {
        log.info("Admin {} permanently deleting material {}", adminId, materialId);

        // 1. Validate admin
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException(adminId));

        if (!admin.isAdmin()) {
            log.warn("User {} attempted to permanently delete material {} without admin rights",
                adminId, materialId);
            throw new UnauthorizedMaterialAccessException("Only admins can permanently delete materials");
        }

        // 2. Get material
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // 3. Delete file from file system
        try {
            fileStorageService.deleteFile(material.getFilePath());
        } catch (Exception e) {
            log.error("Failed to delete file for material {}: {}", materialId, e.getMessage());
            // Continue with database deletion even if file deletion fails
        }

        // 4. Delete from database
        materialRepository.deleteById(materialId);

        log.info("Material {} permanently deleted", materialId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Material> getMaterialsByTeacher(Long teacherId) {
        log.debug("Fetching materials uploaded by teacher {}", teacherId);
        return materialRepository.findByUploadedById(teacherId);
    }
}
