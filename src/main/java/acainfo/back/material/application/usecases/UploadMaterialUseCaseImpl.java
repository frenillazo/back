package acainfo.back.material.application.usecases;

import acainfo.back.material.application.ports.in.UploadMaterialUseCase;
import acainfo.back.material.application.ports.out.MaterialRepositoryPort;
import acainfo.back.material.application.services.FileStorageService;
import acainfo.back.material.domain.exception.InvalidFileTypeException;
import acainfo.back.material.domain.exception.UnauthorizedMaterialAccessException;
import acainfo.back.material.domain.model.MaterialDomain;
import acainfo.back.material.domain.model.MaterialType;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Implementation of UploadMaterialUseCase
 * Handles uploading educational material files
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UploadMaterialUseCaseImpl implements UploadMaterialUseCase {

    private final MaterialRepositoryPort materialRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    public MaterialDomain uploadMaterial(
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

        // 3. Validate subject group exists (repository will throw if not found)
        // This is checked when saving the material

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

        // 6. Create material domain
        MaterialDomain material = MaterialDomain.builder()
                .subjectGroupId(subjectGroupId)
                .fileName(fileName)
                .filePath(filePath)
                .type(type)
                .fileSize(fileSize)
                .description(description)
                .topic(topic)
                .uploadedById(uploaderId)
                .uploadedAt(LocalDateTime.now())
                .requiresPayment(requiresPayment != null ? requiresPayment : true)
                .isActive(true)
                .build();

        // 7. Validate domain
        material.validate();

        // 8. Save to database
        MaterialDomain savedMaterial = materialRepository.save(material);

        log.info("Material uploaded successfully. ID: {}, File: {}, Type: {}, Size: {}",
                savedMaterial.getId(), fileName, type, savedMaterial.getFormattedFileSize());

        return savedMaterial;
    }
}
