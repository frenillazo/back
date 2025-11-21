package acainfo.back.material.infrastructure.adapters.in.rest;

import acainfo.back.material.application.ports.in.DownloadMaterialUseCase;
import acainfo.back.material.application.ports.in.ManageMaterialUseCase;
import acainfo.back.material.application.ports.in.UploadMaterialUseCase;
import acainfo.back.material.domain.model.Material;
import acainfo.back.material.domain.model.MaterialType;
import acainfo.back.material.infrastructure.adapters.in.dto.MaterialResponse;
import acainfo.back.material.infrastructure.adapters.in.dto.UpdateMaterialRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for educational material management.
 * Handles file upload, download, and material metadata operations.
 */
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Materials", description = "Educational material management endpoints")
public class MaterialController {

    private final UploadMaterialUseCase uploadMaterialUseCase;
    private final DownloadMaterialUseCase downloadMaterialUseCase;
    private final ManageMaterialUseCase manageMaterialUseCase;

    // ==================== UPLOAD MATERIAL ====================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Upload a material file",
        description = "Uploads an educational material file to a subject group. " +
                      "Only teachers and admins can upload. " +
                      "Supported file types: PDF, Java, C++, Header files (.h)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Material uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file type or empty file"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Subject group not found"),
        @ApiResponse(responseCode = "500", description = "File storage error")
    })
    public ResponseEntity<MaterialResponse> uploadMaterial(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Subject group ID") @RequestParam Long subjectGroupId,
            @Parameter(description = "Uploader user ID") @RequestParam Long uploaderId,
            @Parameter(description = "Optional description") @RequestParam(required = false) String description,
            @Parameter(description = "Optional topic/unit") @RequestParam(required = false) String topic,
            @Parameter(description = "Requires payment validation") @RequestParam(required = false, defaultValue = "true") Boolean requiresPayment
    ) {
        log.info("Upload request received. File: {}, Group: {}, Uploader: {}",
            file.getOriginalFilename(), subjectGroupId, uploaderId);

        Material material = uploadMaterialUseCase.uploadMaterial(
            file, subjectGroupId, uploaderId, description, topic, requiresPayment
        );

        MaterialResponse response = MaterialResponse.fromEntity(material);

        log.info("Material uploaded successfully with ID: {}", material.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== DOWNLOAD MATERIAL ====================

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Download a material file",
        description = "Downloads the material file. " +
                      "Access control enforced: students need active enrollment, " +
                      "materials with payment requirement need valid payment status (TODO)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - no enrollment or payment required"),
        @ApiResponse(responseCode = "404", description = "Material not found"),
        @ApiResponse(responseCode = "500", description = "File not found in storage")
    })
    public ResponseEntity<Resource> downloadMaterial(
            @Parameter(description = "Material ID") @PathVariable Long id,
            @Parameter(description = "User ID requesting download") @RequestParam Long userId
    ) {
        log.debug("Download request for material {} by user {}", id, userId);

        Resource resource = downloadMaterialUseCase.downloadMaterial(id, userId);
        String fileName = downloadMaterialUseCase.getFileName(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    // ==================== GET MATERIAL ====================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get material by ID",
        description = "Retrieves material metadata by ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Material found"),
        @ApiResponse(responseCode = "404", description = "Material not found")
    })
    public ResponseEntity<MaterialResponse> getMaterialById(
            @Parameter(description = "Material ID") @PathVariable Long id
    ) {
        log.debug("Fetching material by ID: {}", id);

        Material material = manageMaterialUseCase.getMaterialById(id);
        MaterialResponse response = MaterialResponse.fromEntity(material);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get all materials for a subject group",
        description = "Retrieves all active materials for a specific subject group"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Materials retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<MaterialResponse>> getMaterialsByGroup(
            @Parameter(description = "Subject group ID") @PathVariable Long groupId
    ) {
        log.debug("Fetching materials for group {}", groupId);

        List<Material> materials = manageMaterialUseCase.getMaterialsBySubjectGroup(groupId);
        List<MaterialResponse> response = materials.stream()
                .map(MaterialResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}/type")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get materials by subject group and type",
        description = "Retrieves materials filtered by file type (PDF, JAVA, CPP, HEADER)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Materials retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid material type")
    })
    public ResponseEntity<List<MaterialResponse>> getMaterialsByGroupAndType(
            @Parameter(description = "Subject group ID") @PathVariable Long groupId,
            @Parameter(description = "Material type") @RequestParam MaterialType type
    ) {
        log.debug("Fetching materials for group {} and type {}", groupId, type);

        List<Material> materials = manageMaterialUseCase.getMaterialsBySubjectGroupAndType(groupId, type);
        List<MaterialResponse> response = materials.stream()
                .map(MaterialResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/groups/{groupId}/topic")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get materials by subject group and topic",
        description = "Retrieves materials filtered by topic/unit"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Materials retrieved successfully")
    })
    public ResponseEntity<List<MaterialResponse>> getMaterialsByGroupAndTopic(
            @Parameter(description = "Subject group ID") @PathVariable Long groupId,
            @Parameter(description = "Topic/unit name") @RequestParam String topic
    ) {
        log.debug("Fetching materials for group {} and topic '{}'", groupId, topic);

        List<Material> materials = manageMaterialUseCase.getMaterialsBySubjectGroupAndTopic(groupId, topic);
        List<MaterialResponse> response = materials.stream()
                .map(MaterialResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/teachers/{teacherId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get materials uploaded by a teacher",
        description = "Retrieves all materials uploaded by a specific teacher"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Materials retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<List<MaterialResponse>> getMaterialsByTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId
    ) {
        log.debug("Fetching materials uploaded by teacher {}", teacherId);

        List<Material> materials = manageMaterialUseCase.getMaterialsByTeacher(teacherId);
        List<MaterialResponse> response = materials.stream()
                .map(MaterialResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE MATERIAL ====================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Update material metadata",
        description = "Updates material description, topic, or payment requirement. " +
                      "Only the uploader or admin can update."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Material updated successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Material not found")
    })
    public ResponseEntity<MaterialResponse> updateMaterial(
            @Parameter(description = "Material ID") @PathVariable Long id,
            @Parameter(description = "User ID making the update") @RequestParam Long userId,
            @Valid @RequestBody UpdateMaterialRequest request
    ) {
        log.info("User {} updating material {}", userId, id);

        Material material = manageMaterialUseCase.updateMaterial(
            id, userId, request.getDescription(), request.getTopic(), request.getRequiresPayment()
        );

        MaterialResponse response = MaterialResponse.fromEntity(material);

        log.info("Material {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    // ==================== DELETE MATERIAL ====================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(
        summary = "Deactivate material (soft delete)",
        description = "Deactivates the material (sets isActive=false). " +
                      "File is kept in storage. Only the uploader or admin can deactivate."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Material deactivated successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Material not found")
    })
    public ResponseEntity<Void> deactivateMaterial(
            @Parameter(description = "Material ID") @PathVariable Long id,
            @Parameter(description = "User ID requesting deletion") @RequestParam Long userId
    ) {
        log.info("User {} deactivating material {}", userId, id);

        manageMaterialUseCase.deactivateMaterial(id, userId);

        log.info("Material {} deactivated successfully", id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Permanently delete material (hard delete)",
        description = "Permanently deletes the material from database and file system. " +
                      "This action cannot be undone. Only admins can perform this operation."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Material permanently deleted"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions - admin only"),
        @ApiResponse(responseCode = "404", description = "Material not found")
    })
    public ResponseEntity<Void> deleteMaterialPermanently(
            @Parameter(description = "Material ID") @PathVariable Long id,
            @Parameter(description = "Admin user ID") @RequestParam Long adminId
    ) {
        log.info("Admin {} permanently deleting material {}", adminId, id);

        manageMaterialUseCase.deleteMaterialPermanently(id, adminId);

        log.info("Material {} permanently deleted", id);
        return ResponseEntity.noContent().build();
    }
}
