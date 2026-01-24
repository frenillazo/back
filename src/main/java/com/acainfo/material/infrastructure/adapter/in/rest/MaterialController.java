package com.acainfo.material.infrastructure.adapter.in.rest;

import com.acainfo.material.application.dto.MaterialDownload;
import com.acainfo.material.application.dto.MaterialFilters;
import com.acainfo.material.application.dto.UploadMaterialCommand;
import com.acainfo.material.application.port.in.DeleteMaterialUseCase;
import com.acainfo.material.application.port.in.DownloadMaterialUseCase;
import com.acainfo.material.application.port.in.GetMaterialUseCase;
import com.acainfo.material.application.port.in.UploadMaterialUseCase;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.MaterialResponse;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.UploadMaterialRequest;
import com.acainfo.material.infrastructure.adapter.in.rest.mapper.MaterialRestMapper;
import com.acainfo.shared.application.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final DeleteMaterialUseCase deleteMaterialUseCase;
    private final GetMaterialUseCase getMaterialUseCase;
    private final MaterialRestMapper mapper;
    private final MaterialResponseEnricher materialResponseEnricher;

    /**
     * Upload a material file.
     * Requires ADMIN or TEACHER role.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MaterialResponse> upload(
            @Valid @RequestPart("metadata") UploadMaterialRequest request,
            @RequestPart("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId // TODO: Replace with security context
    ) throws IOException {
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
     * All users can see metadata.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaterialResponse> getById(@PathVariable Long id) {
        Material material = getMaterialUseCase.getById(id);
        return ResponseEntity.ok(materialResponseEnricher.enrich(material));
    }

    /**
     * Download material content.
     * Requires proper access (admin/teacher or student with enrollment + payments ok).
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId // TODO: Replace with security context
    ) {
        MaterialDownload download = downloadMaterialUseCase.download(id, userId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .contentLength(download.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.filename() + "\"")
                .body(new InputStreamResource(download.content()));
    }

    /**
     * Delete a material.
     * Requires ADMIN or TEACHER role.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteMaterialUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * List materials with filters.
     */
    @GetMapping
    public ResponseEntity<PageResponse<MaterialResponse>> listWithFilters(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long uploadedById,
            @RequestParam(required = false) String fileExtension,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        MaterialFilters filters = new MaterialFilters(
                subjectId, uploadedById, fileExtension, searchTerm,
                page, size, sortBy, sortDirection
        );

        PageResponse<Material> result = getMaterialUseCase.findWithFilters(filters);
        List<MaterialResponse> content = materialResponseEnricher.enrichList(result.content());

        return ResponseEntity.ok(new PageResponse<>(
                content,
                result.pageNumber(),
                result.pageSize(),
                result.totalElements(),
                result.totalPages(),
                result.first(),
                result.last()
        ));
    }

    /**
     * Get materials for a subject.
     */
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<MaterialResponse>> getBySubjectId(@PathVariable Long subjectId) {
        List<Material> materials = getMaterialUseCase.getBySubjectId(subjectId);
        return ResponseEntity.ok(materialResponseEnricher.enrichList(materials));
    }

    /**
     * Check if current user can download a material.
     */
    @GetMapping("/{id}/can-download")
    public ResponseEntity<CanDownloadResponse> canDownload(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId // TODO: Replace with security context
    ) {
        boolean canDownload = getMaterialUseCase.canDownload(id, userId);
        return ResponseEntity.ok(new CanDownloadResponse(canDownload));
    }

    /**
     * Response DTO for can-download check.
     */
    public record CanDownloadResponse(boolean canDownload) {
    }
}
