package acainfo.back.material.application.ports.in;

import acainfo.back.material.domain.model.Material;
import acainfo.back.material.domain.model.MaterialType;

import java.util.List;

/**
 * Use case for managing materials (CRUD operations).
 * Teachers can manage materials for their subject groups.
 * Admins can manage all materials.
 */
public interface ManageMaterialUseCase {

    /**
     * Gets a material by ID.
     *
     * @param materialId the material ID
     * @return the material
     * @throws acainfo.back.material.domain.exception.MaterialNotFoundException if not found
     */
    Material getMaterialById(Long materialId);

    /**
     * Gets all active materials for a subject group.
     *
     * @param subjectGroupId the subject group ID
     * @return list of active materials
     */
    List<Material> getMaterialsBySubjectGroup(Long subjectGroupId);

    /**
     * Gets materials by subject group and type.
     *
     * @param subjectGroupId the subject group ID
     * @param type the material type
     * @return list of materials
     */
    List<Material> getMaterialsBySubjectGroupAndType(Long subjectGroupId, MaterialType type);

    /**
     * Gets materials by subject group and topic.
     *
     * @param subjectGroupId the subject group ID
     * @param topic the topic/unit
     * @return list of materials
     */
    List<Material> getMaterialsBySubjectGroupAndTopic(Long subjectGroupId, String topic);

    /**
     * Updates material metadata (description, topic, requiresPayment).
     * Only the uploader or admin can update.
     *
     * @param materialId the material ID
     * @param userId the user making the update
     * @param description new description (null to keep current)
     * @param topic new topic (null to keep current)
     * @param requiresPayment new payment requirement (null to keep current)
     * @return the updated material
     * @throws acainfo.back.material.domain.exception.MaterialNotFoundException if not found
     * @throws acainfo.back.shared.domain.exception.UnauthorizedException if user is not authorized
     */
    Material updateMaterial(
        Long materialId,
        Long userId,
        String description,
        String topic,
        Boolean requiresPayment
    );

    /**
     * Deactivates a material (soft delete).
     * Only the uploader or admin can deactivate.
     *
     * @param materialId the material ID
     * @param userId the user requesting deletion
     * @throws acainfo.back.material.domain.exception.MaterialNotFoundException if not found
     * @throws acainfo.back.shared.domain.exception.UnauthorizedException if user is not authorized
     */
    void deactivateMaterial(Long materialId, Long userId);

    /**
     * Permanently deletes a material and its file.
     * Only admins can perform physical deletion.
     *
     * @param materialId the material ID
     * @param adminId the admin ID
     * @throws acainfo.back.material.domain.exception.MaterialNotFoundException if not found
     * @throws acainfo.back.shared.domain.exception.UnauthorizedException if user is not admin
     */
    void deleteMaterialPermanently(Long materialId, Long adminId);

    /**
     * Gets materials uploaded by a specific teacher.
     *
     * @param teacherId the teacher ID
     * @return list of materials
     */
    List<Material> getMaterialsByTeacher(Long teacherId);
}
