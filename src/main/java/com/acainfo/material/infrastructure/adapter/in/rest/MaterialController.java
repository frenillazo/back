package com.acainfo.material.infrastructure.adapter.in.rest;

import com.acainfo.material.application.dto.MaterialDownload;
import com.acainfo.material.application.dto.MaterialFilters;
import com.acainfo.material.application.dto.UpdateMaterialCommand;
import com.acainfo.material.application.dto.UploadMaterialCommand;
import com.acainfo.material.application.port.in.DeleteMaterialUseCase;
import com.acainfo.material.application.port.in.DownloadMaterialUseCase;
import com.acainfo.material.application.port.in.GetMaterialUseCase;
import com.acainfo.material.application.port.in.PreviewMaterialUseCase;
import com.acainfo.material.application.port.in.UpdateMaterialUseCase;
import com.acainfo.material.application.port.in.UploadMaterialUseCase;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.BatchDownloadDisabledRequest;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.BatchUpdateResponse;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.BatchVisibilityRequest;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.MaterialResponse;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.UpdateMaterialRequest;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.UploadMaterialRequest;
import com.acainfo.material.infrastructure.adapter.in.rest.mapper.MaterialRestMapper;
import com.acainfo.security.userdetails.CustomUserDetails;
import com.acainfo.shared.application.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for Material operations.
 *
 * All responses are enriched with related entity data (subject name, uploader name, etc.)
 * to reduce the number of API calls the frontend needs to make.
 */
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final UploadMaterialUseCase uploadMaterialUseCase;
    private final DownloadMaterialUseCase downloadMaterialUseCase;
    private final PreviewMaterialUseCase previewMaterialUseCase;
    private final DeleteMaterialUseCase deleteMaterialUseCase;
    private final GetMaterialUseCase getMaterialUseCase;
    private final UpdateMaterialUseCase updateMaterialUseCase;
    private final MaterialRestMapper mapper;
    private final MaterialResponseEnricher materialResponseEnricher;

    /**
     * Upload a material file.
     * Requires ADMIN or TEACHER role.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<MaterialResponse> upload(
            @Valid @RequestPart("metadata") UploadMaterialRequest request,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        Long userId = userDetails.getUserId();
        UploadMaterialCommand command = new UploadMaterialCommand(
                request.subjectId(),
                userId,
                request.name(),
                request.description(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream(),
                request.getCategoryOrDefault()
        );

        Material material = uploadMaterialUseCase.upload(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(materialResponseEnricher.enrich(material));
    }

    /**
     * Get material metadata by ID.
     * Requires authentication.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MaterialResponse> getById(@PathVariable Long id) {
        Material material = getMaterialUseCase.getById(id);
        return ResponseEntity.ok(materialResponseEnricher.enrich(material));
    }

    /**
     * Download material content.
     * Requires proper access (admin/teacher or student with enrollment + payments ok)
     * AND that the material does not have downloads disabled by an admin.
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        MaterialDownload download = downloadMaterialUseCase.download(id, userId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .contentLength(download.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.filename() + "\"")
                .body(new InputStreamResource(download.content()));
    }

    /**
     * Stream material content for in-browser visualization.
     * Same access rules as download EXCEPT it does not enforce {@code downloadDisabled}:
     * a material whose download is disabled can still be opened in the in-app viewer.
     * Returns the content with {@code Content-Disposition: inline}.
     */
    @GetMapping("/{id}/preview")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> preview(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        MaterialDownload download = previewMaterialUseCase.preview(id, userId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .contentLength(download.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + download.filename() + "\"")
                .body(new InputStreamResource(download.content()));
    }

    /**
     * Delete a material.
     * Requires ADMIN or TEACHER role.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteMaterialUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * List materials with filters.
     * Requires authentication. Non-admin/teacher users only see visible materials.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<MaterialResponse>> listWithFilters(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long uploadedById,
            @RequestParam(required = false) String fileExtension,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MaterialFilters filters = new MaterialFilters(
                subjectId, uploadedById, fileExtension, searchTerm,
                page, size, sortBy, sortDirection
        );

        PageResponse<Material> result = getMaterialUseCase.findWithFilters(filters);
        List<Material> visibleForCaller = filterVisibleForCaller(result.content(), userDetails);
        List<MaterialResponse> content = materialResponseEnricher.enrichList(visibleForCaller);

        return ResponseEntity.ok(new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last(),
                result.empty()
        ));
    }

    /**
     * Get materials for a subject.
     * Requires authentication. Non-admin/teacher users only see visible materials.
     */
    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MaterialResponse>> getBySubjectId(
            @PathVariable Long subjectId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Material> materials = getMaterialUseCase.getBySubjectId(subjectId);
        List<Material> visible = filterVisibleForCaller(materials, userDetails);
        return ResponseEntity.ok(materialResponseEnricher.enrichList(visible));
    }

    /**
     * Hide non-visible materials from non-admin/non-teacher users.
     * Admin and teacher always see everything (so they can manage).
     */
    private List<Material> filterVisibleForCaller(List<Material> materials, CustomUserDetails userDetails) {
        if (userDetails == null || isAdminOrTeacher(userDetails)) {
            return materials;
        }
        return materials.stream().filter(Material::isVisible).toList();
    }

    private boolean isAdminOrTeacher(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .anyMatch(role -> "ROLE_ADMIN".equals(role) || "ROLE_TEACHER".equals(role));
    }

    /**
     * Check if current user can download a material.
     */
    @GetMapping("/{id}/can-download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CanDownloadResponse> canDownload(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        boolean canDownload = getMaterialUseCase.canDownload(id, userId);
        return ResponseEntity.ok(new CanDownloadResponse(canDownload));
    }

    /**
     * Get recent materials for the current student.
     * Returns materials uploaded in the last N days for subjects the student is enrolled in.
     */
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MaterialResponse>> getRecentForStudent(
            @RequestParam(defaultValue = "3") Integer days,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        List<Material> materials = getMaterialUseCase.getRecentForStudent(userId, days);
        // Recent endpoint is for students -> always filter hidden materials
        List<Material> visible = filterVisibleForCaller(materials, userDetails);
        return ResponseEntity.ok(materialResponseEnricher.enrichList(visible));
    }

    /**
     * Update material metadata and/or admin flags (visible / downloadDisabled).
     * Requires ADMIN role.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialResponse> updateMaterial(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMaterialRequest request
    ) {
        UpdateMaterialCommand command = new UpdateMaterialCommand(
                request.name(),
                request.description(),
                request.visible(),
                request.downloadDisabled()
        );
        Material updated = updateMaterialUseCase.updateMetadata(id, command);
        return ResponseEntity.ok(materialResponseEnricher.enrich(updated));
    }

    /**
     * Batch toggle the downloadDisabled flag for several materials.
     * Requires ADMIN role.
     */
    @PatchMapping("/batch/download-disabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BatchUpdateResponse> batchSetDownloadDisabled(
            @Valid @RequestBody BatchDownloadDisabledRequest request
    ) {
        int updated = updateMaterialUseCase.batchSetDownloadDisabled(request.ids(), request.disabled());
        return ResponseEntity.ok(new BatchUpdateResponse(updated));
    }

    /**
     * Batch toggle the visibility for several materials.
     * Requires ADMIN role.
     */
    @PatchMapping("/batch/visibility")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BatchUpdateResponse> batchSetVisibility(
            @Valid @RequestBody BatchVisibilityRequest request
    ) {
        int updated = updateMaterialUseCase.batchSetVisibility(request.ids(), request.visible());
        return ResponseEntity.ok(new BatchUpdateResponse(updated));
    }

    /**
     * Response DTO for can-download check.
     */
    public record CanDownloadResponse(boolean canDownload) {
    }
}
