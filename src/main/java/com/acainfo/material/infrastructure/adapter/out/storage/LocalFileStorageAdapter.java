package com.acainfo.material.infrastructure.adapter.out.storage;

import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.domain.exception.FileStorageException;
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
 * <p>Storage structure (new uploads; legacy files keep their frozen storage_path):</p>
 * <pre>
 * {base-path}/
 *   └── subjects/
 *       └── {subjectCode-subjectName}/
 *           └── {storedFilename}
 * </pre>
 * <p>Example: subjects/ing101-programacion-i/uuid.pdf</p>
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
            throw new FileStorageException("No se pudo inicializar el directorio de almacenamiento: " + basePath, e);
        }
    }

    @Override
    public String store(InputStream content, String storedFilename, Long subjectId) {
        try {
            // Get subject information
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new FileStorageException("Asignatura no encontrada con id: " + subjectId));

            // Build folder structure: subjects/{subjectCode-subjectName}/
            String subjectFolderName = sanitizeFolderName(subject.getCode() + "-" + subject.getName());

            Path subjectDir = basePath.resolve("subjects")
                    .resolve(subjectFolderName);
            Files.createDirectories(subjectDir);

            // Store file
            Path targetPath = subjectDir.resolve(storedFilename);
            Files.copy(content, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path
            String storagePath = "subjects/" + subjectFolderName + "/" + storedFilename;
            log.debug("File stored at: {}", storagePath);

            return storagePath;
        } catch (IOException e) {
            throw new FileStorageException("No se pudo almacenar el archivo: " + storedFilename, e);
        }
    }

    @Override
    public InputStream retrieve(String storagePath) {
        try {
            Path filePath = basePath.resolve(storagePath);

            if (!Files.exists(filePath)) {
                throw new FileStorageException("Archivo no encontrado: " + storagePath);
            }

            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new FileStorageException("No se pudo recuperar el archivo: " + storagePath, e);
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
            throw new FileStorageException("No se pudo eliminar el archivo: " + storagePath, e);
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
