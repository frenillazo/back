package com.acainfo.material.infrastructure.adapter.out.storage;

import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.domain.exception.FileStorageException;
import com.acainfo.material.domain.model.MaterialCategory;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.model.Subject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Local filesystem implementation of FileStoragePort.
 * Used for development and testing environments.
 *
 * <p>Storage structure:</p>
 * <pre>
 * {base-path}/
 *   └── subjects/
 *       └── {subjectCode-subjectName}/
 *           └── {categoryFolder}/
 *               └── {storedFilename}
 * </pre>
 * <p>Example: subjects/ing101-programacion-i/teoria/uuid.pdf</p>
 */
@Slf4j
@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path basePath;
    private final SubjectRepositoryPort subjectRepository;

    public LocalFileStorageAdapter(
            @Value("${app.storage.local.base-path:./storage/materials}") String basePath,
            SubjectRepositoryPort subjectRepository) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        this.subjectRepository = subjectRepository;
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(basePath);
            log.info("Local file storage initialized at: {}", basePath);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage directory: " + basePath, e);
        }
    }

    @Override
    public String store(InputStream content, String storedFilename, Long subjectId, MaterialCategory category) {
        try {
            // Get subject information
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new FileStorageException("Subject not found with id: " + subjectId));

            // Build folder structure: subjects/{subjectCode-subjectName}/{categoryFolder}/
            String subjectFolderName = sanitizeFolderName(subject.getCode() + "-" + subject.getName());
            String categoryFolderName = category.getFolderName();

            Path subjectDir = basePath.resolve("subjects")
                    .resolve(subjectFolderName)
                    .resolve(categoryFolderName);
            Files.createDirectories(subjectDir);

            // Store file
            Path targetPath = subjectDir.resolve(storedFilename);
            Files.copy(content, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path
            String storagePath = "subjects/" + subjectFolderName + "/" + categoryFolderName + "/" + storedFilename;
            log.debug("File stored at: {}", storagePath);

            return storagePath;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + storedFilename, e);
        }
    }

    @Override
    public InputStream retrieve(String storagePath) {
        try {
            Path filePath = basePath.resolve(storagePath);

            if (!Files.exists(filePath)) {
                throw new FileStorageException("File not found: " + storagePath);
            }

            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Failed to retrieve file: " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path filePath = basePath.resolve(storagePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("File deleted: {}", storagePath);
            } else {
                log.warn("File not found for deletion: {}", storagePath);
            }
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + storagePath, e);
        }
    }

    @Override
    public boolean exists(String storagePath) {
        Path filePath = basePath.resolve(storagePath);
        return Files.exists(filePath);
    }

    /**
     * Sanitizes folder names for safe filesystem usage.
     * Removes special characters, replaces spaces with hyphens, and converts to lowercase.
     *
     * @param name Original folder name
     * @return Sanitized folder name
     */
    private String sanitizeFolderName(String name) {
        return name
                .replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .toLowerCase();
    }
}
