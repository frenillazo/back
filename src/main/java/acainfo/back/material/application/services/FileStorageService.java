package acainfo.back.material.application.services;

import acainfo.back.material.domain.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for handling file storage operations in the local file system.
 * Files are organized by subject group ID.
 *
 * Directory structure:
 * {upload-dir}/
 *   {groupId}/
 *     {uuid}_{filename}
 */
@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;

    /**
     * Constructor that initializes the storage location.
     *
     * @param uploadDir the base directory for file uploads (from application.properties)
     */
    public FileStorageService(@Value("${app.file.upload-dir:./uploads/materials}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage initialized at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Stores a file in the file system.
     *
     * @param file the multipart file to store
     * @param subjectGroupId the subject group ID (for organizing files)
     * @return the relative file path (groupId/uuid_filename)
     * @throws FileStorageException if storage fails
     */
    public String storeFile(MultipartFile file, Long subjectGroupId) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Filename contains invalid path sequence: " + originalFileName);
            }

            // Generate unique filename: {uuid}_{originalName}
            String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;

            // Create group directory if it doesn't exist
            Path groupDirectory = this.fileStorageLocation.resolve(String.valueOf(subjectGroupId));
            Files.createDirectories(groupDirectory);

            // Copy file to the target location
            Path targetLocation = groupDirectory.resolve(uniqueFileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // Return relative path: groupId/uuid_filename
            String relativePath = subjectGroupId + "/" + uniqueFileName;
            log.info("File stored successfully: {}", relativePath);

            return relativePath;

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    /**
     * Loads a file as a Resource for download.
     *
     * @param filePath the relative file path (groupId/uuid_filename)
     * @return the file as a Resource
     * @throws FileStorageException if file is not found or cannot be read
     */
    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.debug("File loaded successfully: {}", filePath);
                return resource;
            } else {
                throw new FileStorageException("File not found or not readable: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found: " + filePath, ex);
        }
    }

    /**
     * Deletes a file from the file system.
     *
     * @param filePath the relative file path (groupId/uuid_filename)
     * @throws FileStorageException if deletion fails
     */
    public void deleteFile(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();

            if (Files.exists(file)) {
                Files.delete(file);
                log.info("File deleted successfully: {}", filePath);
            } else {
                log.warn("Attempted to delete non-existent file: {}", filePath);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file: " + filePath, ex);
        }
    }

    /**
     * Checks if a file exists in the storage.
     *
     * @param filePath the relative file path
     * @return true if file exists
     */
    public boolean fileExists(String filePath) {
        Path file = this.fileStorageLocation.resolve(filePath).normalize();
        return Files.exists(file);
    }

    /**
     * Gets the file size in bytes.
     *
     * @param filePath the relative file path
     * @return file size in bytes
     * @throws FileStorageException if file doesn't exist or cannot be read
     */
    public long getFileSize(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            return Files.size(file);
        } catch (IOException ex) {
            throw new FileStorageException("Could not read file size: " + filePath, ex);
        }
    }
}
