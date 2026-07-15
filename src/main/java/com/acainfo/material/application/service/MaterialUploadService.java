package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.UploadMaterialCommand;
import com.acainfo.material.application.port.in.DeleteMaterialUseCase;
import com.acainfo.material.application.port.in.UploadMaterialUseCase;
import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.application.port.out.MaterialFolderRepositoryPort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.FolderSubjectMismatchException;
import com.acainfo.material.domain.exception.InvalidFileTypeException;
import com.acainfo.material.domain.exception.MaterialFolderNotFoundException;
import com.acainfo.material.domain.exception.MaterialNotFoundException;
import com.acainfo.material.domain.model.AcademicYear;
import com.acainfo.material.domain.model.AllowedFileTypes;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.domain.model.MaterialFolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for material upload and delete operations.
 * Implements UploadMaterialUseCase and DeleteMaterialUseCase.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MaterialUploadService implements UploadMaterialUseCase, DeleteMaterialUseCase {

    private final MaterialRepositoryPort materialRepository;
    private final MaterialFolderRepositoryPort materialFolderRepository;
    private final FileStoragePort fileStorage;
    private final Clock clock;

    @Override
    public Material upload(UploadMaterialCommand command) {
        log.debug("Uploading material '{}' for subject {}",
                command.name(), command.subjectId());

        // 1. Validate file type
        String extension = AllowedFileTypes.extractExtension(command.originalFilename());
        if (!AllowedFileTypes.isAllowed(extension)) {
            throw new InvalidFileTypeException(extension, String.join(", ", AllowedFileTypes.ALLOWED_EXTENSIONS));
        }

        // 2. Destination folder (if any) must belong to the material's subject
        validateFolderBelongsToSubject(command.folderId(), command.subjectId());

        // 3. Generate unique stored filename
        String storedFilename = UUID.randomUUID() + "." + extension;

        // 4. Store file
        String storagePath = fileStorage.store(command.content(), storedFilename, command.subjectId());

        // 5. Create and save material metadata
        LocalDateTime now = LocalDateTime.now();
        Material material = Material.builder()
                .subjectId(command.subjectId())
                .uploadedById(command.uploadedById())
                .name(command.name())
                .description(command.description())
                .originalFilename(command.originalFilename())
                .storedFilename(storedFilename)
                .fileExtension(extension)
                .mimeType(command.mimeType())
                .fileSize(command.fileSize())
                .storagePath(storagePath)
                .folderId(command.folderId())
                .academicYear(AcademicYear.current(clock))
                .uploadedAt(now)
                // Newly uploaded materials are visible and downloadable; record activation
                // timestamps so the auto-disable scheduled task can count from upload.
                .visible(true)
                .downloadDisabled(false)
                .visibilityEnabledAt(now)
                .downloadEnabledAt(now)
                .build();

        Material saved = materialRepository.save(material);
        log.info("Material uploaded: id={}, name='{}', subject={}",
                saved.getId(), saved.getName(), saved.getSubjectId());

        return saved;
    }

    /**
     * Validate that the destination folder (when provided) belongs to the given subject.
     */
    private void validateFolderBelongsToSubject(Long folderId, Long subjectId) {
        if (folderId == null) {
            return;
        }
        MaterialFolder folder = materialFolderRepository.findById(folderId)
                .orElseThrow(() -> new MaterialFolderNotFoundException(folderId));
        if (!folder.getSubjectId().equals(subjectId)) {
            throw new FolderSubjectMismatchException(folderId, subjectId);
        }
    }

    @Override
    public void delete(Long materialId) {
        log.debug("Deleting material {}", materialId);

        // 1. Find material
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // 2. Delete file from storage
        fileStorage.delete(material.getStoragePath());

        // 3. Delete metadata
        materialRepository.delete(materialId);

        log.info("Material deleted: id={}, name='{}'", materialId, material.getName());
    }
}
