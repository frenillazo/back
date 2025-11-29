package acainfo.back.material.application.ports.in;

import acainfo.back.material.domain.model.Material;
import acainfo.back.user.domain.exception.UnauthorizedException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Use case for uploading educational material files.
 * Only teachers can upload materials to their subject groups.
 */
public interface UploadMaterialUseCase {

    /**
     * Uploads a material file to a subject group.
     *
     * @param file the file to upload
     * @param subjectGroupId the subject group ID
     * @param uploaderId the teacher ID who uploads the file
     * @param description optional description of the material
     * @param topic optional topic/unit classification
     * @param requiresPayment whether this material requires payment validation
     * @return the created material entity
     * @throws acainfo.back.material.domain.exception.InvalidFileTypeException if file type is not supported
     * @throws acainfo.back.material.domain.exception.FileStorageException if file storage fails
     * @throws acainfo.back.subjectgroup.domain.exception.GroupNotFoundException if group doesn't exist
     * @throws UnauthorizedException if uploader is not a teacher
     */
    Material uploadMaterial(
        MultipartFile file,
        Long subjectGroupId,
        Long uploaderId,
        String description,
        String topic,
        Boolean requiresPayment
    );
}
