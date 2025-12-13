package com.acainfo.material.infrastructure.adapter.out.storage;

import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.domain.exception.FileStorageException;
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
 *       └── {subjectId}/
 *           └── {storedFilename}
 * </pre>
 */
@Slf4j
@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path basePath;

    public LocalFileStorageAdapter(
            @Value("${app.storage.local.base-path:./storage/materials}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
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
    public String store(InputStream content, String storedFilename, Long subjectId) {
        try {
            // Create subject directory if not exists
            Path subjectDir = basePath.resolve("subjects").resolve(String.valueOf(subjectId));
            Files.createDirectories(subjectDir);

            // Store file
            Path targetPath = subjectDir.resolve(storedFilename);
            Files.copy(content, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path
            String storagePath = "subjects/" + subjectId + "/" + storedFilename;
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
}
