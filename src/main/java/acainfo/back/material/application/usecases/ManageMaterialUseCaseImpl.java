package acainfo.back.material.application.usecases;

import acainfo.back.material.application.ports.in.ManageMaterialUseCase;
import acainfo.back.material.application.ports.out.MaterialRepositoryPort;
import acainfo.back.material.application.services.FileStorageService;
import acainfo.back.material.domain.exception.MaterialNotFoundException;
import acainfo.back.material.domain.exception.UnauthorizedMaterialAccessException;
import acainfo.back.material.domain.model.MaterialDomain;
import acainfo.back.material.domain.model.MaterialType;
import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.domain.model.UserDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of ManageMaterialUseCase
 * Handles CRUD operations for materials
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManageMaterialUseCaseImpl implements ManageMaterialUseCase {

    private final MaterialRepositoryPort materialRepository;
    private final UserRepositoryPort userRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public MaterialDomain getMaterialById(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDomain> getMaterialsBySubjectGroup(Long subjectGroupId) {
        log.debug("Fetching active materials for group {}", subjectGroupId);
        return materialRepository.findBySubjectGroupIdAndIsActiveTrue(subjectGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDomain> getMaterialsBySubjectGroupAndType(Long subjectGroupId, MaterialType type) {
        log.debug("Fetching materials for group {} and type {}", subjectGroupId, type);
        return materialRepository.findBySubjectGroupIdAndTypeAndIsActiveTrue(subjectGroupId, type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDomain> getMaterialsBySubjectGroupAndTopic(Long subjectGroupId, String topic) {
        log.debug("Fetching materials for group {} and topic '{}'", subjectGroupId, topic);
        return materialRepository.findBySubjectGroupIdAndTopicAndIsActiveTrue(subjectGroupId, topic);
    }

    @Override
    @Transactional
    public MaterialDomain updateMaterial(
            Long materialId,
            Long userId,
            String description,
            String topic,
            Boolean requiresPayment
    ) {
        log.info("User {} updating material {}", userId, materialId);

        // 1. Get material
        MaterialDomain material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // 2. Validate user is authorized (uploader or admin)
        UserDomain user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.isAdmin() && !material.getUploadedById().equals(userId)) {
            log.warn("User {} attempted to update material {} without authorization", userId, materialId);
            throw new UnauthorizedMaterialAccessException("Only the uploader or admin can update this material");
        }

        // 3. Update fields using domain methods
        MaterialDomain updated = material;

        if (description != null) {
            updated = updated.updateDescription(description);
        }
        if (topic != null) {
            updated = updated.updateTopic(topic);
        }
        if (requiresPayment != null) {
            updated = updated.updateRequiresPayment(requiresPayment);
        }

        // 4. Save
        MaterialDomain saved = materialRepository.save(updated);
        log.info("Material {} updated successfully", materialId);

        return saved;
    }

    @Override
    @Transactional
    public void deactivateMaterial(Long materialId, Long userId) {
        log.info("User {} deactivating material {}", userId, materialId);

        // 1. Get material
        MaterialDomain material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // 2. Validate user is authorized
        UserDomain user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.isAdmin() && !material.getUploadedById().equals(userId)) {
            log.warn("User {} attempted to deactivate material {} without authorization", userId, materialId);
            throw new UnauthorizedMaterialAccessException("Only the uploader or admin can deactivate this material");
        }

        // 3. Deactivate (soft delete) using domain method
        MaterialDomain deactivated = material.deactivate();
        materialRepository.save(deactivated);

        log.info("Material {} deactivated successfully", materialId);
    }

    @Override
    @Transactional
    public void deleteMaterialPermanently(Long materialId, Long adminId) {
        log.info("Admin {} permanently deleting material {}", adminId, materialId);

        // 1. Validate admin
        UserDomain admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException(adminId));

        if (!admin.isAdmin()) {
            log.warn("User {} attempted to permanently delete material {} without admin rights",
                    adminId, materialId);
            throw new UnauthorizedMaterialAccessException("Only admins can permanently delete materials");
        }

        // 2. Get material
        MaterialDomain material = materialRepository.findById(materialId)
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
    public List<MaterialDomain> getMaterialsByTeacher(Long teacherId) {
        log.debug("Fetching materials uploaded by teacher {}", teacherId);
        return materialRepository.findByUploadedById(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getTopicsBySubjectGroup(Long subjectGroupId) {
        log.debug("Fetching distinct topics for group {}", subjectGroupId);
        return materialRepository.findDistinctTopicsBySubjectGroupId(subjectGroupId);
    }
}
