package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.UploadMaterialCommand;
import com.acainfo.material.application.port.in.DeleteMaterialUseCase;
import com.acainfo.material.application.port.in.UploadMaterialUseCase;
import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.InvalidFileTypeException;
import com.acainfo.material.domain.exception.MaterialNotFoundException;
import com.acainfo.material.domain.model.AllowedFileTypes;
import com.acainfo.material.domain.model.Material;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FileStoragePort fileStorage;

    @Override
    public Material upload(UploadMaterialCommand command) {
        log.debug("Uploading material '{}' for subject {} with category {}",
                command.name(), command.subjectId(), command.category());

        // 1. Validate file type
        String extension = AllowedFileTypes.extractExtension(command.originalFilename());
        if (!AllowedFileTypes.isAllowed(extension)) {
            throw new InvalidFileTypeException(extension, String.join(", ", AllowedFileTypes.ALLOWED_EXTENSIONS));
        }

        // 2. Generate unique stored filename
        String storedFilename = UUID.randomUUID() + "." + extension;

        // 3. Store file
        String storagePath = fileStorage.store(command.content(), storedFilename, command.subjectId(), command.category());

        // 4. Create and save material metadata
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
                .category(command.category())
                .uploadedAt(LocalDateTime.now())
                .build();

        Material saved = materialRepository.save(material);
        log.info("Material uploaded: id={}, name='{}', subject={}, category={}",
                saved.getId(), saved.getName(), saved.getSubjectId(), saved.getCategory());

        return saved;
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
