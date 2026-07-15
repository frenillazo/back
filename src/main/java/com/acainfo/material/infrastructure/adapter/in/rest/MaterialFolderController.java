package com.acainfo.material.infrastructure.adapter.in.rest;

import com.acainfo.material.application.dto.UpdateMaterialFolderCommand;
import com.acainfo.material.application.port.in.MaterialFolderUseCase;
import com.acainfo.material.domain.model.MaterialFolder;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.CreateMaterialFolderRequest;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.MaterialFolderResponse;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.UpdateMaterialFolderRequest;
import com.acainfo.material.infrastructure.adapter.in.rest.mapper.MaterialFolderRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for MaterialFolder operations.
 * Folders are per-subject (single level); listing is nested under the subject,
 * item operations are top-level by folder id.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MaterialFolderController {

    private final MaterialFolderUseCase materialFolderUseCase;
    private final MaterialFolderRestMapper mapper;

    /**
     * List the folders of a subject ordered by position.
     * Requires authentication.
     */
    @GetMapping("/subjects/{subjectId}/material-folders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MaterialFolderResponse>> getBySubjectId(@PathVariable Long subjectId) {
        List<MaterialFolder> folders = materialFolderUseCase.getBySubjectId(subjectId);
        return ResponseEntity.ok(mapper.toResponseList(folders));
    }

    /**
     * Create a folder for a subject.
     * Requires ADMIN role.
     */
    @PostMapping("/subjects/{subjectId}/material-folders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialFolderResponse> create(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreateMaterialFolderRequest request
    ) {
        MaterialFolder created = materialFolderUseCase.create(subjectId, request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    /**
     * Rename and/or reorder a folder.
     * Requires ADMIN role.
     */
    @PatchMapping("/material-folders/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialFolderResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMaterialFolderRequest request
    ) {
        UpdateMaterialFolderCommand command =
                new UpdateMaterialFolderCommand(request.name(), request.position());
        MaterialFolder updated = materialFolderUseCase.update(id, command);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    /**
     * Delete a folder. Its materials go back to the subject root.
     * Requires ADMIN role.
     */
    @DeleteMapping("/material-folders/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        materialFolderUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
